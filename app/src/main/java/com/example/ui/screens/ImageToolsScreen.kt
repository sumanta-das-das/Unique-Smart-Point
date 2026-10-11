package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RotateRight
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToolsScreen(
    currentUserId: String,
    repository: FirestoreRepository?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTool by remember { mutableStateOf("Compress Image") }
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Parameters
    var compressQuality by remember { mutableFloatStateOf(75f) }
    var targetWidth by remember { mutableStateOf("800") }
    var targetHeight by remember { mutableStateOf("600") }
    var watermarkText by remember { mutableStateOf("USP WATERMARK") }
    var selectedFilter by remember { mutableStateOf("Original") }

    LaunchedEffect(Unit) {
        try {
            val sample = BitmapFactory.decodeResource(context.resources, R.drawable.banner_passport)
            sourceBitmap = sample
            processedBitmap = sample
        } catch (e: Exception) {}
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bmp = ImageProcessor.loadBitmapFromUri(context, it)
            if (bmp != null) {
                sourceBitmap = bmp
                processedBitmap = bmp
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Complete Image Tools", fontWeight = FontWeight.Bold) },
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
                .testTag("image_tools_screen")
        ) {
            // Tool selection chips
            val tools = listOf("Compress Image", "Resize Pixels", "Crop / Rotate", "Watermark", "Color Palette")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tools.take(3).forEach { tool ->
                    FilterChip(
                        selected = selectedTool == tool,
                        onClick = { selectedTool = tool },
                        label = { Text(tool, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tools.drop(3).forEach { tool ->
                    FilterChip(
                        selected = selectedTool == tool,
                        onClick = { selectedTool = tool },
                        label = { Text(tool, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Preview Canvas
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (processedBitmap != null) {
                        Image(
                            bitmap = processedBitmap!!.asImageBitmap(),
                            contentDescription = "Image preview",
                            modifier = Modifier
                                .padding(10.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Text("Select image to begin", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { picker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick Image from Device")
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-tool Controls
            when (selectedTool) {
                "Compress Image" -> {
                    Text("Compression Quality: ${compressQuality.toInt()}%", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Slider(
                        value = compressQuality,
                        onValueChange = { compressQuality = it },
                        valueRange = 10f..100f,
                        modifier = Modifier.testTag("compress_slider")
                    )
                    Text("Original size: ~${(sourceBitmap?.byteCount ?: 0) / 1024} KB  ➔  Target: ~${((sourceBitmap?.byteCount ?: 0) * (compressQuality / 100f) * 0.25f).toInt()} KB", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                "Resize Pixels" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = targetWidth,
                            onValueChange = { targetWidth = it },
                            label = { Text("Width (px)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = targetHeight,
                            onValueChange = { targetHeight = it },
                            label = { Text("Height (px)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                "Crop / Rotate" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val src = processedBitmap ?: sourceBitmap ?: return@OutlinedButton
                                processedBitmap = ImageProcessor.rotate(src, 90f)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rotate 90°")
                        }
                        OutlinedButton(
                            onClick = {
                                val src = processedBitmap ?: sourceBitmap ?: return@OutlinedButton
                                processedBitmap = ImageProcessor.flip(src, horizontal = true)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Flip, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Flip Horiz")
                        }
                    }
                }
                "Watermark" -> {
                    OutlinedTextField(
                        value = watermarkText,
                        onValueChange = { watermarkText = it },
                        label = { Text("Watermark Text") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                "Color Palette" -> {
                    val palette = remember(processedBitmap) {
                        processedBitmap?.let { ImageProcessor.extractPalette(it) } ?: emptyList()
                    }
                    Text("Dominant Hex Colors in Photo:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        palette.forEach { c ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(c))
                                        .border(1.dp, Color.Gray, CircleShape)
                                )
                                Text(
                                    text = String.format("#%06X", (0xFFFFFF and c)),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action: Apply & Save
            Button(
                onClick = {
                    val src = sourceBitmap ?: return@Button
                    val result = when (selectedTool) {
                        "Resize Pixels" -> {
                            val w = targetWidth.toIntOrNull() ?: 800
                            val h = targetHeight.toIntOrNull() ?: 600
                            ImageProcessor.resizeByPixels(src, w, h)
                        }
                        "Watermark" -> {
                            ImageProcessor.addWatermark(src, watermarkText)
                        }
                        else -> {
                            processedBitmap ?: src
                        }
                    }
                    processedBitmap = result

                    val file = ImageProcessor.saveBitmapToCache(
                        context,
                        result,
                        "usp_img_${System.currentTimeMillis()}.png"
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar("Success! Saved as: ${file.name}")
                        if (currentUserId.isNotBlank() && repository != null) {
                            repository.recordProcessingHistory(
                                ProcessingHistoryItem(
                                    toolName = selectedTool,
                                    category = "Image Tools",
                                    status = "Success",
                                    summary = "Processed ${file.name}"
                                )
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_apply_image_tool"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Process & Save Result", fontWeight = FontWeight.Bold)
            }
        }
    }
}
