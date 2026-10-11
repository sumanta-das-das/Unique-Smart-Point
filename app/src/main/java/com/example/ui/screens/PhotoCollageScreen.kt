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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateListOf
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
import com.example.utils.PdfUtils
import kotlinx.coroutines.launch

data class CollageLayoutOption(
    val id: String,
    val name: String,
    val photoCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCollageScreen(
    currentUserId: String,
    repository: FirestoreRepository?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val layouts = remember {
        listOf(
            CollageLayoutOption("4_grid", "4 Grid (2x2)", 4),
            CollageLayoutOption("3_grid", "3 Grid Hero", 3),
            CollageLayoutOption("2_split_v", "2 Split Vertical", 2),
            CollageLayoutOption("2_split_h", "2 Split Horizontal", 2)
        )
    }

    var selectedLayout by remember { mutableStateOf(layouts[0]) }
    val photoList = remember { mutableStateListOf<Bitmap>() }
    var spacingPx by remember { mutableFloatStateOf(16f) }
    var borderBgColor by remember { mutableIntStateOf(AndroidColor.WHITE) }
    var renderedCollageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Load initial sample photos
    LaunchedEffect(Unit) {
        try {
            val b1 = BitmapFactory.decodeResource(context.resources, R.drawable.banner_passport)
            val b2 = BitmapFactory.decodeResource(context.resources, R.drawable.banner_ai)
            val b3 = BitmapFactory.decodeResource(context.resources, R.drawable.banner_pdf)
            val b4 = BitmapFactory.decodeResource(context.resources, R.drawable.banner_collage)
            if (photoList.isEmpty()) {
                if (b1 != null) photoList.add(b1)
                if (b2 != null) photoList.add(b2)
                if (b3 != null) photoList.add(b3)
                if (b4 != null) photoList.add(b4)
            }
        } catch (e: Exception) {}
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            val bmp = ImageProcessor.loadBitmapFromUri(context, uri)
            if (bmp != null) {
                photoList.add(bmp)
            }
        }
    }

    // Recompute collage when layout, photos, spacing, or color changes
    LaunchedEffect(photoList.size, selectedLayout, spacingPx, borderBgColor) {
        if (photoList.isEmpty()) return@LaunchedEffect
        val collage = ImageProcessor.createCollage(
            bitmaps = photoList.toList(),
            layout = selectedLayout.id,
            spacingPx = spacingPx.toInt(),
            backgroundColor = borderBgColor,
            canvasWidth = 1080,
            canvasHeight = 1080
        )
        renderedCollageBitmap = collage
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Photo Collage Studio", fontWeight = FontWeight.Bold) },
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
                .testTag("photo_collage_screen")
        ) {
            // Live Collage Canvas Preview
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (renderedCollageBitmap != null) {
                        Image(
                            bitmap = renderedCollageBitmap!!.asImageBitmap(),
                            contentDescription = "Photo Collage Canvas",
                            modifier = Modifier
                                .padding(12.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Text("Add photos to generate collage", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Add Photos Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { photoPicker.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_add_collage_photos"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Photos (${photoList.size})")
                }

                if (photoList.size > 2) {
                    OutlinedButton(
                        onClick = {
                            if (photoList.isNotEmpty()) {
                                photoList.removeAt(photoList.lastIndex)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Clear Last")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Select Collage Layout
            Text("Choose Layout", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                layouts.take(2).forEach { layout ->
                    FilterChip(
                        selected = selectedLayout.id == layout.id,
                        onClick = { selectedLayout = layout },
                        label = { Text(layout.name, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                layouts.drop(2).forEach { layout ->
                    FilterChip(
                        selected = selectedLayout.id == layout.id,
                        onClick = { selectedLayout = layout },
                        label = { Text(layout.name, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Border Spacing Slider
            Text("Border Spacing (${spacingPx.toInt()} px)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Slider(
                value = spacingPx,
                onValueChange = { spacingPx = it },
                valueRange = 0f..40f,
                modifier = Modifier.testTag("collage_spacing_slider")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Border Color Options
            Text("Border Background Color", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val bgColors = listOf(
                    "White" to AndroidColor.WHITE,
                    "Black" to AndroidColor.BLACK,
                    "Festive Crimson" to AndroidColor.rgb(190, 24, 93),
                    "Sapphire Blue" to AndroidColor.rgb(30, 64, 175),
                    "Warm Cream" to AndroidColor.rgb(254, 243, 199)
                )

                bgColors.forEach { (name, colorInt) ->
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .border(
                                width = if (borderBgColor == colorInt) 3.dp else 1.dp,
                                color = if (borderBgColor == colorInt) PrimaryBlue else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable { borderBgColor = colorInt },
                        contentAlignment = Alignment.Center
                    ) {
                        if (borderBgColor == colorInt) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = if (colorInt == AndroidColor.WHITE || colorInt == AndroidColor.rgb(254, 243, 199)) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons: Save Image, Export PDF, and Save to Cloud
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val bmp = renderedCollageBitmap ?: return@Button
                        val file = ImageProcessor.saveBitmapToCache(
                            context,
                            bmp,
                            "collage_${System.currentTimeMillis()}.png"
                        )
                        scope.launch {
                            snackbarHostState.showSnackbar("Collage saved: ${file.name}")
                            if (currentUserId.isNotBlank() && repository != null) {
                                repository.recordProcessingHistory(
                                    ProcessingHistoryItem(
                                        toolName = "Photo Collage Maker",
                                        category = "Photo Services",
                                        status = "Success",
                                        summary = "Created ${selectedLayout.name} collage"
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_download_collage"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Image", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val bmp = renderedCollageBitmap ?: return@Button
                        val file = PdfUtils.createPdfFromImages(
                            context,
                            listOf(bmp),
                            "collage_sheet_${System.currentTimeMillis()}.pdf"
                        )
                        scope.launch {
                            snackbarHostState.showSnackbar("Exported Collage PDF: ${file.name}")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_pdf_collage"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (currentUserId.isBlank() || repository == null) {
                            scope.launch { snackbarHostState.showSnackbar("Sign in with Google to save designs to cloud") }
                            return@Button
                        }
                        scope.launch {
                            repository.saveDesign(
                                SavedDesignItem(
                                    title = "Collage - ${selectedLayout.name}",
                                    toolType = "photo_collage",
                                    previewData = "${photoList.size} photos collage"
                                )
                            )
                            snackbarHostState.showSnackbar("Collage saved to your account!")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_save_cloud_collage"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cloud", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
