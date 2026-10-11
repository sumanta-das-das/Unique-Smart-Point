package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceOrder
import com.example.data.repository.FirestoreRepository
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SuccessGreen
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

data class ManualServiceItem(
    val name: String,
    val category: String,
    val priceInr: Long,
    val turnAround: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    user: FirebaseUser?,
    repository: FirestoreRepository?,
    onBack: () -> Unit,
    onViewOrders: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val services = remember {
        listOf(
            ManualServiceItem("Professional Photo Retouching", "Photo Services", 49L, "Same Day"),
            ManualServiceItem("Custom Photo Frame High-Res Print", "Creative Studio", 149L, "1-2 Days"),
            ManualServiceItem("Online Govt / SSC Exam Form Assistance", "Digital Services", 99L, "Immediate"),
            ManualServiceItem("PAN / Aadhaar Update Form Guidance", "Digital Services", 79L, "Same Day"),
            ManualServiceItem("Bengali Festival Custom Poster & Banner", "Creative Studio", 199L, "1-2 Days")
        )
    }

    var selectedService by remember { mutableStateOf(services[0]) }
    var customerName by remember { mutableStateOf(user?.displayName ?: "") }
    var customerEmail by remember { mutableStateOf(user?.email ?: "") }
    var customerPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var lastSubmittedOrderId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Service Booking Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("booking_screen")
        ) {
            Text(
                text = "Book Digital Center Services",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Professional studio editing, print-ready frames & online document assistance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Select Service
            Text("Select Desired Service", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                services.forEach { service ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedService.name == service.name)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedService = service }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = service.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${service.category} • Delivery: ${service.turnAround}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "₹${service.priceInr}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer Contact Details Form
            Text("Customer & Delivery Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Full Name *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_name_input"),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customerEmail,
                onValueChange = { customerEmail = it },
                label = { Text("Email Address *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_email_input"),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customerPhone,
                onValueChange = { customerPhone = it },
                label = { Text("WhatsApp / Mobile Phone *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_phone_input"),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Special Instructions or Details") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .testTag("booking_notes_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Transparent Fee Summary Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Service Fee Payable", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${selectedService.priceInr} (Inclusive of GST)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text("Pay Upon Delivery", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    if (user == null) {
                        scope.launch { snackbarHostState.showSnackbar("Please sign in with Google to submit orders") }
                        return@Button
                    }
                    if (customerName.isBlank() || customerEmail.isBlank() || customerPhone.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please fill in your name, email, and contact number") }
                        return@Button
                    }
                    isSubmitting = true
                    scope.launch {
                        val orderId = "USP-${(10000..99999).random()}"
                        val order = ServiceOrder(
                            orderId = orderId,
                            userId = user.uid,
                            customerName = customerName,
                            customerEmail = customerEmail,
                            serviceName = selectedService.name,
                            serviceCategory = selectedService.category,
                            status = "pending",
                            notes = "Phone: $customerPhone | $notes",
                            fileAttachmentName = "customer_request.jpg",
                            price = selectedService.priceInr
                        )
                        val res = repository?.createOrder(order)
                        isSubmitting = false
                        if (res?.isSuccess == true) {
                            lastSubmittedOrderId = orderId
                            snackbarHostState.showSnackbar("Order $orderId placed successfully!")
                        } else {
                            snackbarHostState.showSnackbar("Failed to place order: ${res?.exceptionOrNull()?.message}")
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_confirm_order"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submitting Order...")
                } else {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm & Submit Order", fontWeight = FontWeight.Bold)
                }
            }

            // Order Confirmation Card
            if (lastSubmittedOrderId != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Order Submitted Successfully!", fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Order ID: $lastSubmittedOrderId", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = PrimaryBlue)
                        Text("You can track progress and updates directly in your user dashboard.", fontSize = 12.sp, color = Color(0xFF166534))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onViewOrders,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("View in My Orders")
                        }
                    }
                }
            }
        }
    }
}
