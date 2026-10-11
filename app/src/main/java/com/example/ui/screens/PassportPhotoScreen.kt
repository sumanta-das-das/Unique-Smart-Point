package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.R
import com.example.data.model.ProcessingHistoryItem
import com.example.data.repository.FirestoreRepository
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryPurple
import com.example.utils.ImageProcessor
import com.example.utils.PdfUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassportPhotoScreen(
    currentUserId: String,
    repository: FirestoreRepository?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedPreset by remember { mutableStateOf("India (3.5 x 4.5 cm)") }
    var selectedBgColor by remember { mutableIntStateOf(AndroidColor.WHITE) }
    var isSheetMode by remember { mutableStateOf(false) } // False = Single, True = 4x6 Print Sheet
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Load sample portrait if none chosen initially
    LaunchedEffect(Unit) {
        try {
            val sample = BitmapFactory.decodeResource(context.resources, R.drawable.banner_passport)
            sourceBitmap = sample
        } catch (e: Exception) {}
    }

    // Photo picker launcher (zero permission standard)
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bmp = ImageProcessor.loadBitmapFromUri(context, it)
            if (bmp != null) {
                sourceBitmap = bmp
            }
        }
    }

    // Recompute processed photo whenever source, preset, background, or mode changes
    LaunchedEffect(sourceBitmap, selectedPreset, selectedBgColor, isSheetMode) {
        val src = sourceBitmap ?: return@LaunchedEffect
        val single = ImageProcessor.createPassportPhoto(
            source = src,
            targetWidthPx = 413,
            targetHeightPx = 531,
            backgroundColor = selectedBgColor,
            addBorder = true
        )
        processedBitmap = if (isSheetMode) {
            ImageProcessor.generate4x6PrintSheet(single)
        } else {
            single
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Passport Photo Maker", fontWeight = FontWeight.Bold) },
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
                .testTag("passport_photo_screen")
        ) {
            // Preview Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (processedBitmap != null) {
                        Image(
                            bitmap = processedBitmap!!.asImageBitmap(),
                            contentDescription = "Passport Photo Preview",
                            modifier = Modifier
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Text("Upload a photo to begin", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Upload / Pick Button
            OutlinedButton(
                onClick = { photoPicker.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_upload_photo"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload Photo from Device", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Selector: Single Photo vs 4x6 Sheet (8 photos)
            Text("Output Layout", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = !isSheetMode,
                    onClick = { isSheetMode = false },
                    label = { Text("Single Photo") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = isSheetMode,
                    onClick = { isSheetMode = true },
                    label = { Text("4×6 Print Sheet (8-up)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Presets
            Text("Select Dimension Specification", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            val presets = listOf(
                "India (3.5 x 4.5 cm)",
                "PAN Card (2.5 x 3.5 cm)",
                "US Visa (2 x 2 inch)",
                "SSC / Exam Portal"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.take(2).forEach { p ->
                    FilterChip(
                        selected = selectedPreset == p,
                        onClick = { selectedPreset = p },
                        label = { Text(p, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.drop(2).forEach { p ->
                    FilterChip(
                        selected = selectedPreset == p,
                        onClick = { selectedPreset = p },
                        label = { Text(p, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Background Color Options
            Text("Background Color", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val bgColors = listOf(
                    "White" to AndroidColor.WHITE,
                    "Light Blue" to AndroidColor.rgb(179, 217, 255),
                    "Studio Gray" to AndroidColor.rgb(235, 235, 235),
                    "Deep Blue" to AndroidColor.rgb(20, 60, 140)
                )

                bgColors.forEach { (name, colorInt) ->
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .border(
                                width = if (selectedBgColor == colorInt) 3.dp else 1.dp,
                                color = if (selectedBgColor == colorInt) PrimaryBlue else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable { selectedBgColor = colorInt },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedBgColor == colorInt) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = if (colorInt == AndroidColor.WHITE) Color.Black else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Export Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val bmp = processedBitmap ?: return@Button
                        val file = ImageProcessor.saveBitmapToCache(
                            context,
                            bmp,
                            "passport_${System.currentTimeMillis()}.png"
                        )
                        successMessage = "Saved image to: ${file.name}"
                        scope.launch {
                            snackbarHostState.showSnackbar("Successfully generated passport photo!")
                            if (currentUserId.isNotBlank() && repository != null) {
                                repository.recordProcessingHistory(
                                    ProcessingHistoryItem(
                                        toolName = "Passport Photo Maker",
                                        category = "Photo Services",
                                        status = "Success",
                                        summary = "Created $selectedPreset layout (${if (isSheetMode) "4x6 sheet" else "single"})"
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_save_photo"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Image", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val bmp = processedBitmap ?: return@Button
                        val file = PdfUtils.createPdfFromImages(
                            context,
                            listOf(bmp),
                            "passport_sheet_${System.currentTimeMillis()}.pdf"
                        )
                        successMessage = "Saved PDF to: ${file.name}"
                        scope.launch {
                            snackbarHostState.showSnackbar("Print-ready PDF generated successfully!")
                            if (currentUserId.isNotBlank() && repository != null) {
                                repository.recordProcessingHistory(
                                    ProcessingHistoryItem(
                                        toolName = "Passport Print PDF",
                                        category = "Photo Services",
                                        status = "Success",
                                        summary = "Generated PDF sheet (${file.name})"
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_save_pdf"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF", fontWeight = FontWeight.Bold)
                }
            }

            if (successMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ $successMessage",
                        color = Color(0xFF15803D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
