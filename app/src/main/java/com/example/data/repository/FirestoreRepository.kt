package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.ProcessingHistoryItem
import com.example.data.model.SavedDesignItem
import com.example.data.model.ServiceOrder
import com.example.data.model.SupportTicketItem
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

class FirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun getOrCreateUserProfile(uid: String, displayName: String, email: String): UserProfile {
        val docRef = db.collection("users").document(uid)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                if (profile != null) return profile
            }
            // Create user profile
            val isBootstrapAdmin = email == "sumantadas425@gmail.com"
            val role = if (isBootstrapAdmin) "admin" else "customer"
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "displayName" to displayName.ifBlank { "User" },
                "email" to email,
                "role" to role,
                "credits" to 25L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            return UserProfile(
                userId = uid,
                displayName = displayName.ifBlank { "User" },
                email = email,
                role = role,
                credits = 25L
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            return UserProfile(userId = uid, displayName = displayName, email = email)
        }
    }

    fun observeOrders(userId: String, isAdmin: Boolean): Flow<List<ServiceOrder>> = flow {
        val query: Query = if (isAdmin) {
            db.collection("orders").orderBy("createdAt", Query.Direction.DESCENDING)
        } else {
            db.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
        }

        query.snapshots()
            .map { snap -> snap.toObjects(ServiceOrder::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "orders")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun createOrder(order: ServiceOrder): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("orders").document(order.orderId)
        val payload = hashMapOf(
            "orderId" to order.orderId,
            "userId" to uid,
            "customerName" to order.customerName,
            "customerEmail" to order.customerEmail,
            "serviceName" to order.serviceName,
            "serviceCategory" to order.serviceCategory,
            "status" to "pending",
            "notes" to order.notes,
            "fileAttachmentName" to order.fileAttachmentName,
            "price" to order.price,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(order.orderId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        val docRef = db.collection("orders").document(orderId)
        return try {
            docRef.update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeProcessingHistory(userId: String): Flow<List<ProcessingHistoryItem>> = flow {
        val query = db.collection("processing_history")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        query.snapshots()
            .map { snap -> snap.toObjects(ProcessingHistoryItem::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "processing_history")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun recordProcessingHistory(item: ProcessingHistoryItem) {
        try {
            val uid = requireUserId()
            val id = if (item.id.isBlank()) "hist_${System.currentTimeMillis()}" else item.id
            val docRef = db.collection("processing_history").document(id)
            val payload = hashMapOf(
                "id" to id,
                "userId" to uid,
                "toolName" to item.toolName,
                "category" to item.category,
                "status" to item.status,
                "summary" to item.summary,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "processing_history")
        }
    }

    fun observeSavedDesigns(userId: String): Flow<List<SavedDesignItem>> = flow {
        val query = db.collection("saved_designs")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        query.snapshots()
            .map { snap -> snap.toObjects(SavedDesignItem::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "saved_designs")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun saveDesign(design: SavedDesignItem): Result<String> {
        val uid = requireUserId()
        val id = if (design.id.isBlank()) "des_${System.currentTimeMillis()}" else design.id
        val docRef = db.collection("saved_designs").document(id)
        val payload = hashMapOf(
            "id" to id,
            "userId" to uid,
            "title" to design.title,
            "toolType" to design.toolType,
            "previewData" to design.previewData,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeSupportTickets(userId: String, isAdmin: Boolean): Flow<List<SupportTicketItem>> = flow {
        val query = if (isAdmin) {
            db.collection("support_tickets").orderBy("createdAt", Query.Direction.DESCENDING)
        } else {
            db.collection("support_tickets")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
        }

        query.snapshots()
            .map { snap -> snap.toObjects(SupportTicketItem::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "support_tickets")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun submitSupportTicket(ticket: SupportTicketItem): Result<String> {
        val uid = requireUserId()
        val id = if (ticket.id.isBlank()) "tkt_${System.currentTimeMillis()}" else ticket.id
        val docRef = db.collection("support_tickets").document(id)
        val payload = hashMapOf(
            "id" to id,
            "userId" to uid,
            "name" to ticket.name,
            "email" to ticket.email,
            "subject" to ticket.subject,
            "message" to ticket.message,
            "status" to "open",
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }
}
