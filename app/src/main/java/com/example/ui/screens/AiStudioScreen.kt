package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ProcessingHistoryItem
import com.example.data.repository.AiService
import com.example.data.repository.FirestoreRepository
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryPurple
import com.example.utils.ImageProcessor
import com.example.utils.PdfUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStudioScreen(
    currentUserId: String,
    repository: FirestoreRepository?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val aiService = remember { AiService() }

    var selectedMode by remember { mutableStateOf("Photo Enhancer") }
    var userPrompt by remember { mutableStateOf("Formal leave application to branch manager for 3 days") }
    var generatedTextResult by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        try {
            val sample = BitmapFactory.decodeResource(context.resources, R.drawable.banner_ai)
            sourceBitmap = sample
            processedBitmap = sample
        } catch (e: Exception) {}
    }

    val photoPicker = rememberLauncherForActivityResult(
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
                title = { Text("AI Creative Studio", fontWeight = FontWeight.Bold) },
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
                .testTag("ai_studio_screen")
        ) {
            // Mode Selectors
            val modes = listOf("Photo Enhancer", "Art & Sketch", "Letter Writer", "Resume Maker", "OCR Extraction")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modes.take(3).forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = {
                            selectedMode = mode
                            if (mode == "Letter Writer") userPrompt = "Formal leave application in English and Bengali"
                            if (mode == "Resume Maker") userPrompt = "Generate complete professional Biodata / Resume for job"
                            if (mode == "OCR Extraction") userPrompt = "Extract text from scanned document"
                        },
                        label = { Text(mode, fontSize = 11.sp) },
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
                modes.drop(3).forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = {
                            selectedMode = mode
                            if (mode == "Resume Maker") userPrompt = "Generate complete professional Biodata / Resume for job"
                            if (mode == "OCR Extraction") userPrompt = "Extract text from scanned document"
                        },
                        label = { Text(mode, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Visual Photo Modes vs Text/Document AI Modes
            if (selectedMode in listOf("Photo Enhancer", "Art & Sketch")) {
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
                                contentDescription = "AI Preview",
                                modifier = Modifier
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Text("Upload a photo to enhance", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { photoPicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Photo for AI Processing")
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val src = sourceBitmap ?: return@Button
                        isGenerating = true
                        scope.launch {
                            val filtered = if (selectedMode == "Photo Enhancer") {
                                ImageProcessor.applyFilter(src, "Vibrant")
                            } else {
                                ImageProcessor.applyFilter(src, "Pencil Sketch")
                            }
                            processedBitmap = filtered
                            isGenerating = false
                            val file = ImageProcessor.saveBitmapToCache(
                                context,
                                filtered,
                                "ai_${selectedMode.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}.png"
                            )
                            snackbarHostState.showSnackbar("AI processing completed! Saved as: ${file.name}")
                            if (currentUserId.isNotBlank() && repository != null) {
                                repository.recordProcessingHistory(
                                    ProcessingHistoryItem(
                                        toolName = "AI $selectedMode",
                                        category = "AI Studio",
                                        status = "Success",
                                        summary = "Enhanced photo with AI filters"
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_run_ai_photo"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Processing...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply AI Magic", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Text, Letter, Resume & OCR Studio
                OutlinedTextField(
                    value = userPrompt,
                    onValueChange = { userPrompt = it },
                    label = { Text("What would you like the AI to generate?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("ai_prompt_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isGenerating = true
                        scope.launch {
                            val result = aiService.generateAiContent(userPrompt)
                            generatedTextResult = result
                            isGenerating = false
                            snackbarHostState.showSnackbar("AI content generated successfully!")
                            if (currentUserId.isNotBlank() && repository != null) {
                                repository.recordProcessingHistory(
                                    ProcessingHistoryItem(
                                        toolName = selectedMode,
                                        category = "AI Studio",
                                        status = "Success",
                                        summary = "Generated AI content for: ${userPrompt.take(40)}"
                                    )
                                )
                            }
                        }
                    },
                    enabled = !isGenerating && userPrompt.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_run_ai_text"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI is generating...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate with AI", fontWeight = FontWeight.Bold)
                    }
                }

                if (generatedTextResult.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Generated Output", fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                Row {
                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(generatedTextResult))
                                        scope.launch { snackbarHostState.showSnackbar("Copied to clipboard!") }
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                    }
                                    IconButton(onClick = {
                                        val file = PdfUtils.createPdfFromText(
                                            context,
                                            title = selectedMode,
                                            bodyText = generatedTextResult,
                                            filename = "ai_${selectedMode.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
                                        )
                                        scope.launch { snackbarHostState.showSnackbar("Exported PDF: ${file.name}") }
                                    }) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF")
                                    }
                                }
                            }
                            Text(
                                text = generatedTextResult,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
