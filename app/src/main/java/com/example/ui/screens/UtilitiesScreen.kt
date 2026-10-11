package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue
import com.example.utils.ImageProcessor
import com.example.utils.QrUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilitiesScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf("QR Code") }

    // QR & Barcode state
    var qrContent by remember { mutableStateOf("https://uniquesmartpoint.in") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var barcodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Age Calculator state
    var birthYear by remember { mutableStateOf("2000") }
    var birthMonth by remember { mutableStateOf("05") }
    var birthDay by remember { mutableStateOf("15") }
    var calculatedAgeText by remember { mutableStateOf("") }

    // Percentage Calculator state
    var percentValue by remember { mutableStateOf("18") }
    var totalAmount by remember { mutableStateOf("1500") }
    var calculatedPercentText by remember { mutableStateOf("") }

    // Generate QR initially
    LaunchedEffect(qrContent, selectedTab) {
        if (selectedTab == "QR Code") {
            qrBitmap = QrUtils.generateQrBitmap(qrContent)
        } else if (selectedTab == "Barcode") {
            barcodeBitmap = QrUtils.generateBarcodeBitmap(qrContent)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("QR & Digital Utilities", fontWeight = FontWeight.Bold) },
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
                .testTag("utilities_screen")
        ) {
            val tabs = listOf("QR Code", "Barcode", "Age Calc", "Percentage")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            when (selectedTab) {
                "QR Code" -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap!!.asImageBitmap(),
                                    contentDescription = "Generated QR Code",
                                    modifier = Modifier
                                        .size(230.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = qrContent,
                        onValueChange = { qrContent = it },
                        label = { Text("URL, Text, Wi-Fi or UPI ID") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("qr_content_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val bmp = qrBitmap ?: return@Button
                            val file = ImageProcessor.saveBitmapToCache(
                                context,
                                bmp,
                                "qr_${System.currentTimeMillis()}.png"
                            )
                            scope.launch { snackbarHostState.showSnackbar("QR Code saved: ${file.name}") }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_save_qr"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download QR Code Image")
                    }
                }

                "Barcode" -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (barcodeBitmap != null) {
                                Image(
                                    bitmap = barcodeBitmap!!.asImageBitmap(),
                                    contentDescription = "Generated Barcode",
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = qrContent,
                        onValueChange = { qrContent = it },
                        label = { Text("Product Code / Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("barcode_content_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val bmp = barcodeBitmap ?: return@Button
                            val file = ImageProcessor.saveBitmapToCache(
                                context,
                                bmp,
                                "barcode_${System.currentTimeMillis()}.png"
                            )
                            scope.launch { snackbarHostState.showSnackbar("Barcode saved: ${file.name}") }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download Barcode Image")
                    }
                }

                "Age Calc" -> {
                    Text("Date of Birth", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = birthDay,
                            onValueChange = { birthDay = it },
                            label = { Text("Day (DD)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = birthMonth,
                            onValueChange = { birthMonth = it },
                            label = { Text("Month (MM)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = birthYear,
                            onValueChange = { birthYear = it },
                            label = { Text("Year (YYYY)") },
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            try {
                                val d = birthDay.toInt()
                                val m = birthMonth.toInt()
                                val y = birthYear.toInt()
                                val dob = LocalDate.of(y, m, d)
                                val today = LocalDate.now()
                                val period = Period.between(dob, today)
                                calculatedAgeText = "${period.years} Years, ${period.months} Months, and ${period.days} Days old"
                            } catch (e: Exception) {
                                calculatedAgeText = "Invalid Date! Please enter valid Day, Month, and Year."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_calc_age"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calculate Exact Age")
                    }

                    if (calculatedAgeText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Age Result:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(calculatedAgeText, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                "Percentage" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = percentValue,
                            onValueChange = { percentValue = it },
                            label = { Text("Percentage (%)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = totalAmount,
                            onValueChange = { totalAmount = it },
                            label = { Text("Total Amount") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val p = percentValue.toDoubleOrNull() ?: 0.0
                            val t = totalAmount.toDoubleOrNull() ?: 0.0
                            val res = (p / 100.0) * t
                            val totalWith = t + res
                            val totalWithout = t - res
                            calculatedPercentText = "$p% of $t = ${String.format("%.2f", res)}\nWith Addition: ${String.format("%.2f", totalWith)} | With Discount: ${String.format("%.2f", totalWithout)}"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calculate Percentage")
                    }

                    if (calculatedPercentText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(calculatedPercentText, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}
