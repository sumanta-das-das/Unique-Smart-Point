package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
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
import com.example.data.model.SavedDesignItem
import com.example.data.repository.FirestoreRepository
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryPurple
import com.example.utils.ImageProcessor
import kotlinx.coroutines.launch

data class FrameTemplate(
    val id: String,
    val name: String,
    val borderHex: Int,
    val innerHex: Int,
    val defaultText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoFrameScreen(
    currentUserId: String,
    repository: FirestoreRepository?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val templates = remember {
        listOf(
            FrameTemplate("bengali_festive", "Bengali Festive", AndroidColor.rgb(180, 25, 25), AndroidColor.rgb(255, 230, 200), "শুভ উৎসবের শুভেচ্ছা"),
            FrameTemplate("birthday", "Birthday Special", AndroidColor.rgb(120, 50, 200), AndroidColor.rgb(240, 220, 255), "Happy Birthday!"),
            FrameTemplate("wedding", "Wedding & Golden", AndroidColor.rgb(212, 175, 55), AndroidColor.rgb(255, 250, 230), "Together Forever"),
            FrameTemplate("modern_cyan", "Modern Minimal", AndroidColor.rgb(6, 148, 162), AndroidColor.rgb(220, 250, 252), "Special Memories"),
            FrameTemplate("floral_pink", "Floral Rose", AndroidColor.rgb(219, 39, 119), AndroidColor.rgb(253, 232, 240), "With Love")
        )
    }

    var selectedTemplate by remember { mutableStateOf(templates[0]) }
    var customText by remember { mutableStateOf(templates[0].defaultText) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var renderedFrameBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        try {
            val sample = BitmapFactory.decodeResource(context.resources, R.drawable.banner_ai)
            sourceBitmap = sample
        } catch (e: Exception) {}
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bmp = ImageProcessor.loadBitmapFromUri(context, it)
            if (bmp != null) sourceBitmap = bmp
        }
    }

    // Render frame whenever image, template, text, or zoom changes
    LaunchedEffect(sourceBitmap, selectedTemplate, customText, zoomScale) {
        val src = sourceBitmap ?: return@LaunchedEffect
        val canvasSize = 800
        val output = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Draw Outer Frame Border
        val borderPaint = Paint().apply {
            color = selectedTemplate.borderHex
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(0f, 0f, canvasSize.toFloat(), canvasSize.toFloat(), 30f, 30f, borderPaint)

        // Draw Inner Matting / Card
        val innerMargin = 40f
        val matPaint = Paint().apply {
            color = selectedTemplate.innerHex
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            innerMargin,
            innerMargin,
            canvasSize - innerMargin,
            canvasSize - innerMargin,
            20f,
            20f,
            matPaint
        )

        // Photo Window
        val photoMargin = 70f
        val photoBottom = canvasSize - 130f
        val photoRect = RectF(photoMargin, photoMargin, canvasSize - photoMargin, photoBottom)

        // Scaled Photo
        val srcScaled = Bitmap.createScaledBitmap(
            src,
            (photoRect.width() * zoomScale).toInt().coerceAtLeast(10),
            (photoRect.height() * zoomScale).toInt().coerceAtLeast(10),
            true
        )
        val srcRect = Rect(0, 0, (photoRect.width() * zoomScale).toInt(), (photoRect.height() * zoomScale).toInt())

        canvas.save()
        canvas.clipRect(photoRect)
        canvas.drawBitmap(srcScaled, null, photoRect, null)
        canvas.restore()

        // Border around photo window
        val frameOutline = Paint().apply {
            color = selectedTemplate.borderHex
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRect(photoRect, frameOutline)

        // Custom Title / Greeting Text at bottom
        val textPaint = Paint().apply {
            color = selectedTemplate.borderHex
            textSize = 34f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(customText, canvasSize / 2f, canvasSize - 55f, textPaint)

        // Brand watermark at very bottom
        val brandPaint = Paint().apply {
            color = AndroidColor.argb(120, 100, 100, 100)
            textSize = 14f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("UNIQUE SMART POINT STUDIO", canvasSize / 2f, canvasSize - 25f, brandPaint)

        renderedFrameBitmap = output
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Photo Frame Studio", fontWeight = FontWeight.Bold) },
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
                .testTag("photo_frame_screen")
        ) {
            // Live Frame Canvas Preview
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (renderedFrameBitmap != null) {
                        Image(
                            bitmap = renderedFrameBitmap!!.asImageBitmap(),
                            contentDescription = "Custom Photo Frame",
                            modifier = Modifier
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Upload Photo Button
            OutlinedButton(
                onClick = { photoPicker.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_upload_frame_photo"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Personal Photo", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Frame Templates
            Text("Select Frame Design", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                templates.take(3).forEach { t ->
                    FilterChip(
                        selected = selectedTemplate.id == t.id,
                        onClick = {
                            selectedTemplate = t
                            customText = t.defaultText
                        },
                        label = { Text(t.name, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                templates.drop(3).forEach { t ->
                    FilterChip(
                        selected = selectedTemplate.id == t.id,
                        onClick = {
                            selectedTemplate = t
                            customText = t.defaultText
                        },
                        label = { Text(t.name, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Custom Text Input
            OutlinedTextField(
                value = customText,
                onValueChange = { customText = it },
                label = { Text("Custom Greeting or Name Text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("frame_text_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Zoom slider
            Text("Photo Zoom (${(zoomScale * 100).toInt()}%)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Slider(
                value = zoomScale,
                onValueChange = { zoomScale = it },
                valueRange = 0.8f..2.0f,
                modifier = Modifier.testTag("frame_zoom_slider")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Save Image & Save Design to Cloud
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val bmp = renderedFrameBitmap ?: return@Button
                        val file = ImageProcessor.saveBitmapToCache(
                            context,
                            bmp,
                            "frame_${System.currentTimeMillis()}.png"
                        )
                        scope.launch {
                            snackbarHostState.showSnackbar("Frame exported: ${file.name}")
                            if (currentUserId.isNotBlank() && repository != null) {
                                repository.recordProcessingHistory(
                                    ProcessingHistoryItem(
                                        toolName = "Photo Frame Maker",
                                        category = "Creative Design",
                                        status = "Success",
                                        summary = "Created '${selectedTemplate.name}' frame"
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_export_frame"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download Frame")
                }

                Button(
                    onClick = {
                        if (currentUserId.isBlank() || repository == null) {
                            scope.launch { snackbarHostState.showSnackbar("Please sign in to save designs to your cloud account") }
                            return@Button
                        }
                        scope.launch {
                            repository.saveDesign(
                                SavedDesignItem(
                                    title = customText.ifBlank { selectedTemplate.name },
                                    toolType = "photo_frame",
                                    previewData = selectedTemplate.name
                                )
                            )
                            snackbarHostState.showSnackbar("Design saved to your account!")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_save_design_cloud"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Cloud")
                }
            }
        }
    }
}
