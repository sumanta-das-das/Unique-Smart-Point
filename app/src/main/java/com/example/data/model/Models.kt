package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = "customer",
    val credits: Long = 10L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ServiceOrder(
    @DocumentId val orderId: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val serviceName: String = "",
    val serviceCategory: String = "",
    val status: String = "pending", // pending, in_progress, completed, cancelled
    val notes: String = "",
    val fileAttachmentName: String = "",
    val price: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ProcessingHistoryItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val toolName: String = "",
    val category: String = "",
    val status: String = "Success",
    val summary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SavedDesignItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val toolType: String = "", // photo_frame, passport_photo, etc.
    val previewData: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SupportTicketItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val subject: String = "",
    val message: String = "",
    val status: String = "open", // open, resolved
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class ToolDefinition(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val isPopular: Boolean = false,
    val isAi: Boolean = false,
    val iconName: String = "star"
)

data class ServiceCategory(
    val id: String,
    val name: String,
    val description: String,
    val toolCount: Int,
    val colorHex: Long
)
