package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AuthManager
import com.example.data.repository.FirestoreRepository
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AiStudioScreen
import com.example.ui.screens.AllToolsScreen
import com.example.ui.screens.BookingScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocumentToolsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImageToolsScreen
import com.example.ui.screens.PassportPhotoScreen
import com.example.ui.screens.PdfToolsScreen
import com.example.ui.screens.PhotoCollageScreen
import com.example.ui.screens.PhotoFrameScreen
import com.example.ui.screens.PricingContactScreen
import com.example.ui.screens.UtilitiesScreen
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppRoot()
            }
        }
    }
}

@Composable
fun MainAppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager(context) }
    val repository = remember {
        try {
            FirestoreRepository(context)
        } catch (e: Exception) {
            null
        }
    }

    val currentUser by authManager.currentUser.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentScreen by remember { mutableStateOf("home") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        authManager.attemptAutoSignIn(scope)
    }

    // Handle back button to return to home if not on home
    BackHandler(enabled = currentScreen != "home") {
        currentScreen = "home"
    }

    val isAdmin = currentUser?.email == "sumantadas425@gmail.com"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
                    .testTag("navigation_drawer")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Header in drawer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrandGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "UNIQUE SMART POINT",
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryBlue,
                                fontSize = 15.sp
                            )
                            Text("Digital Service Platform", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    val menuItems = listOf(
                        Triple("home", "Home", Icons.Default.Home),
                        Triple("all_tools", "All Tools Catalog", Icons.Default.Build),
                        Triple("tool_collage", "Photo Collage Studio", Icons.Default.GridView),
                        Triple("tool_passport", "Passport Photo Maker", Icons.Default.ContactPage),
                        Triple("frames", "Photo Frame Studio", Icons.Default.Image),
                        Triple("ai_studio", "AI Creative Studio", Icons.Default.AutoAwesome),
                        Triple("tool_pdf", "PDF & Document Tools", Icons.Default.PictureAsPdf),
                        Triple("tool_image", "Image Tools", Icons.Default.Image),
                        Triple("tool_doc", "Text & Resume Tools", Icons.Default.Description),
                        Triple("tool_qr", "QR & Utilities", Icons.Default.QrCode),
                        Triple("booking", "Service Booking", Icons.Default.ShoppingCart),
                        Triple("dashboard", "User Dashboard", Icons.Default.Dashboard),
                        Triple("pricing_contact", "Pricing & Support", Icons.Default.Payments)
                    )

                    menuItems.forEach { (route, title, icon) ->
                        NavigationDrawerItem(
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(title, fontSize = 13.sp) },
                            selected = currentScreen == route,
                            onClick = {
                                currentScreen = route
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedTextColor = PrimaryBlue,
                                selectedIconColor = PrimaryBlue
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    if (isAdmin) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = PrimaryPurple) },
                            label = { Text("Admin Panel", fontWeight = FontWeight.Bold, color = PrimaryPurple) },
                            selected = currentScreen == "admin",
                            onClick = {
                                currentScreen = "admin"
                                scope.launch { drawerState.close() }
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentScreen in listOf("home", "all_tools")) {
                    AppTopBar(
                        user = currentUser,
                        onOpenSearch = { currentScreen = "all_tools" },
                        onOpenMenu = { scope.launch { drawerState.open() } },
                        onProfileClick = { currentScreen = "dashboard" }
                    )
                }
            },
            bottomBar = {
                AppBottomBar(
                    currentDestination = currentScreen,
                    onNavigate = { route -> currentScreen = route }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    "home" -> HomeScreen(
                        onNavigateToTool = { toolId ->
                            currentScreen = when (toolId) {
                                "tool_collage", "photo_collage" -> "tool_collage"
                                "passport_photo", "signature_resizer", "id_card_photo" -> "tool_passport"
                                "photo_frame" -> "frames"
                                "img_to_pdf", "merge_pdf", "compress_pdf", "split_pdf", "rotate_pdf", "watermark_pdf", "tool_pdf_img", "tool_pdf_merge", "tool_pdf_compress", "tool_pdf_watermark" -> "tool_pdf"
                                "compress_img", "resize_pixels", "resize_percent", "crop_image", "palette_extract", "convert_format" -> "tool_image"
                                "ai_bg_remove", "ai_enhance", "ai_ocr", "ai_bengali", "ai_art" -> "ai_studio"
                                "doc_resume", "doc_letter", "word_counter" -> "tool_doc"
                                "qr_code", "barcode", "age_calc" -> "tool_qr"
                                "service_booking", "booking_retouch", "booking_print", "booking_exam" -> "booking"
                                else -> "all_tools"
                            }
                        },
                        onNavigateCategory = { catId ->
                            selectedCategoryFilter = catId
                            currentScreen = "all_tools"
                        },
                        onExploreAll = { currentScreen = "all_tools" }
                    )

                    "all_tools" -> AllToolsScreen(
                        initialCategory = selectedCategoryFilter,
                        onNavigateToTool = { toolId ->
                            currentScreen = when (toolId) {
                                "tool_collage", "photo_collage" -> "tool_collage"
                                "passport_photo", "signature_resizer", "id_card_photo" -> "tool_passport"
                                "photo_frame" -> "frames"
                                "img_to_pdf", "merge_pdf", "compress_pdf", "split_pdf", "rotate_pdf", "watermark_pdf", "tool_pdf_img", "tool_pdf_merge", "tool_pdf_compress", "tool_pdf_watermark" -> "tool_pdf"
                                "compress_img", "resize_pixels", "resize_percent", "crop_image", "palette_extract", "convert_format" -> "tool_image"
                                "ai_bg_remove", "ai_enhance", "ai_ocr", "ai_bengali", "ai_art" -> "ai_studio"
                                "doc_resume", "doc_letter", "word_counter" -> "tool_doc"
                                "qr_code", "barcode", "age_calc" -> "tool_qr"
                                "service_booking", "booking_retouch", "booking_print", "booking_exam" -> "booking"
                                else -> "all_tools"
                            }
                        },
                        onBack = { currentScreen = "home" }
                    )

                    "tool_collage" -> PhotoCollageScreen(
                        currentUserId = currentUser?.uid ?: "",
                        repository = repository,
                        onBack = { currentScreen = "home" }
                    )

                    "ai_studio" -> AiStudioScreen(
                        currentUserId = currentUser?.uid ?: "",
                        repository = repository,
                        onBack = { currentScreen = "home" }
                    )

                    "tool_passport" -> PassportPhotoScreen(
                        currentUserId = currentUser?.uid ?: "",
                        repository = repository,
                        onBack = { currentScreen = "home" }
                    )

                    "frames" -> PhotoFrameScreen(
                        currentUserId = currentUser?.uid ?: "",
                        repository = repository,
                        onBack = { currentScreen = "home" }
                    )

                    "tool_pdf" -> PdfToolsScreen(
                        currentUserId = currentUser?.uid ?: "",
                        repository = repository,
                        onBack = { currentScreen = "home" }
                    )

                    "tool_image" -> ImageToolsScreen(
                        currentUserId = currentUser?.uid ?: "",
                        repository = repository,
                        onBack = { currentScreen = "home" }
                    )

                    "tool_doc" -> DocumentToolsScreen(
                        onBack = { currentScreen = "home" }
                    )

                    "tool_qr" -> UtilitiesScreen(
                        onBack = { currentScreen = "home" }
                    )

                    "booking" -> BookingScreen(
                        user = currentUser,
                        repository = repository,
                        onBack = { currentScreen = "home" },
                        onViewOrders = { currentScreen = "dashboard" }
                    )

                    "dashboard" -> DashboardScreen(
                        user = currentUser,
                        authManager = authManager,
                        repository = repository,
                        onBack = { currentScreen = "home" },
                        onOpenAdmin = { currentScreen = "admin" },
                        onNavigateToTool = { toolId ->
                            currentScreen = when (toolId) {
                                "tool_collage", "photo_collage" -> "tool_collage"
                                "passport_photo", "signature_resizer", "id_card_photo" -> "tool_passport"
                                "photo_frame" -> "frames"
                                "img_to_pdf", "merge_pdf", "compress_pdf", "split_pdf", "rotate_pdf", "watermark_pdf", "tool_pdf_img", "tool_pdf_merge", "tool_pdf_compress", "tool_pdf_watermark" -> "tool_pdf"
                                "compress_img", "resize_pixels", "resize_percent", "crop_image", "palette_extract", "convert_format" -> "tool_image"
                                "ai_bg_remove", "ai_enhance", "ai_ocr", "ai_bengali", "ai_art" -> "ai_studio"
                                "doc_resume", "doc_letter", "word_counter" -> "tool_doc"
                                "qr_code", "barcode", "age_calc" -> "tool_qr"
                                "service_booking", "booking_retouch", "booking_print", "booking_exam" -> "booking"
                                else -> "all_tools"
                            }
                        }
                    )

                    "admin" -> AdminScreen(
                        user = currentUser,
                        repository = repository,
                        onBack = { currentScreen = "dashboard" }
                    )

                    "pricing_contact" -> PricingContactScreen(
                        user = currentUser,
                        repository = repository,
                        onBack = { currentScreen = "home" }
                    )
                }
            }
        }
    }
}
