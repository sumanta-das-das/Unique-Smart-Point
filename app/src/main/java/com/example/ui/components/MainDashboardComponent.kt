package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SuccessGreen

/**
 * Data representation of a service item in the main dashboard grid.
 */
data class DashboardServiceItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: String, // "photo", "pdf", "booking"
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val badge: String? = null
)

/**
 * Complete, modern Main Dashboard UI Component for UNIQUE SMART POINT.
 * Displays:
 * 1. An interactive Photo Collage showcase with live layout previews & instant creation
 * 2. Service category filters (Photo Editing, PDF Tools, Booking, All)
 * 3. Responsive 2-column grid of vibrant service cards with icons, badges and touch targets.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainDashboardComponent(
    onNavigateToService: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("all") }
    var selectedCollageLayout by remember { mutableStateOf("4_grid") }

    val serviceItems = remember {
        listOf(
            // PHOTO EDITING SERVICES
            DashboardServiceItem(
                id = "tool_collage",
                name = "Photo Collage",
                subtitle = "Split, grid & mosaic multi-photo layouts",
                category = "photo",
                icon = Icons.Default.GridView,
                gradientColors = listOf(Color(0xFFE11D48), Color(0xFFF43F5E)),
                badge = "NEW"
            ),
            DashboardServiceItem(
                id = "passport_photo",
                name = "Passport Photos",
                subtitle = "3.5x4.5cm photos & 4x6 print sheets",
                category = "photo",
                icon = Icons.Default.ContactPage,
                gradientColors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6)),
                badge = "POPULAR"
            ),
            DashboardServiceItem(
                id = "photo_frame",
                name = "Photo Frame Studio",
                subtitle = "Festive, birthday & wedding frames",
                category = "photo",
                icon = Icons.Default.Image,
                gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF9333EA)),
                badge = "FEATURED"
            ),
            DashboardServiceItem(
                id = "resize_pixels",
                name = "Image Resizer & Comp",
                subtitle = "Pixel resize & custom compression",
                category = "photo",
                icon = Icons.Default.Crop,
                gradientColors = listOf(Color(0xFF0891B2), Color(0xFF06B6D4)),
                badge = "HD"
            ),
            DashboardServiceItem(
                id = "ai_enhance",
                name = "AI Photo Enhancer",
                subtitle = "Studio clarity & background removal",
                category = "photo",
                icon = Icons.Default.AutoAwesome,
                gradientColors = listOf(Color(0xFF9333EA), Color(0xFFC084FC)),
                badge = "AI"
            ),

            // PDF TOOLS SERVICES
            DashboardServiceItem(
                id = "img_to_pdf",
                name = "Image to PDF",
                subtitle = "Convert multiple photos to single PDF",
                category = "pdf",
                icon = Icons.Default.PictureAsPdf,
                gradientColors = listOf(Color(0xFFDC2626), Color(0xFFEF4444)),
                badge = "FAST"
            ),
            DashboardServiceItem(
                id = "merge_pdf",
                name = "Merge PDF Files",
                subtitle = "Combine multi-page PDF documents",
                category = "pdf",
                icon = Icons.Default.Merge,
                gradientColors = listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6)),
                badge = "PAGES"
            ),
            DashboardServiceItem(
                id = "compress_pdf",
                name = "Compress PDF",
                subtitle = "Reduce document size for portal uploads",
                category = "pdf",
                icon = Icons.Default.Compress,
                gradientColors = listOf(Color(0xFF059669), Color(0xFF10B981)),
                badge = "-80%"
            ),
            DashboardServiceItem(
                id = "watermark_pdf",
                name = "Watermark PDF",
                subtitle = "Add ownership & confidential stamps",
                category = "pdf",
                icon = Icons.Default.Description,
                gradientColors = listOf(Color(0xFFD97706), Color(0xFFF59E0B)),
                badge = "STAMP"
            ),

            // SERVICE BOOKING
            DashboardServiceItem(
                id = "booking_retouch",
                name = "Photo Retouch Booking",
                subtitle = "Professional manual photo editing",
                category = "booking",
                icon = Icons.Default.CameraAlt,
                gradientColors = listOf(Color(0xFF4F46E5), Color(0xFF6366F1)),
                badge = "₹49"
            ),
            DashboardServiceItem(
                id = "booking_print",
                name = "Photo Frame Printing",
                subtitle = "High-res custom frames delivered",
                category = "booking",
                icon = Icons.Default.Print,
                gradientColors = listOf(Color(0xFFBE185D), Color(0xFFDB2777)),
                badge = "₹149"
            ),
            DashboardServiceItem(
                id = "booking_exam",
                name = "Govt / Exam Forms",
                subtitle = "Online application assistance",
                category = "booking",
                icon = Icons.Default.ShoppingBag,
                gradientColors = listOf(Color(0xFF0F766E), Color(0xFF14B8A6)),
                badge = "₹99"
            ),
            DashboardServiceItem(
                id = "service_booking",
                name = "Express Service Desk",
                subtitle = "Custom requests & doorstep delivery",
                category = "booking",
                icon = Icons.Default.Star,
                gradientColors = listOf(Color(0xFFEAB308), Color(0xFFF59E0B)),
                badge = "FAST"
            )
        )
    }

    val filteredServices = remember(selectedFilter) {
        if (selectedFilter == "all") serviceItems
        else serviceItems.filter { it.category == selectedFilter }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("main_dashboard_component")
    ) {
        // ----------------------------------------------------
        // 1. PHOTO COLLAGE FEATURED SHOWCASE CARD
        // ----------------------------------------------------
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_photo_collage_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFFE11D48), Color(0xFFF43F5E)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Photo Collage Icon",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Photo Collage Studio",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE11D48)
                                ) {
                                    Text(
                                        text = "NEW",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Combine multiple pictures into creative layouts & grids",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // High-resolution Hero Banner Visual for Photo Collage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigateToService("tool_collage") }
                        .testTag("collage_banner_preview")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.banner_collage),
                        contentDescription = "Photo Collage Studio Showcase",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay gradient for contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                )
                            )
                    )

                    // Text & layout indicators over banner
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Multi-Photo Story & Print Layouts",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "2x2 Grid • 3-Photo Hero • Split Mosaic • Instant PDF/JPG Export",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Mini Collage Layout Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val layoutChips = listOf(
                        "4_grid" to "4 Grid (2x2)",
                        "3_grid" to "3 Hero",
                        "2_split_v" to "2 Split (V)",
                        "2_split_h" to "2 Split (H)"
                    )
                    layoutChips.forEach { (id, label) ->
                        FilterChip(
                            selected = selectedCollageLayout == id,
                            onClick = { selectedCollageLayout = id },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = PrimaryBlue
                            ),
                            modifier = Modifier.testTag("collage_layout_chip_$id")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mini Interactive Collage Preview Wireframe Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(6.dp)
                ) {
                    when (selectedCollageLayout) {
                        "4_grid" -> {
                            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color(0xFFE11D48).copy(alpha = 0.35f)), contentAlignment = Alignment.Center) { Text("Photo 1", fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color(0xFF2563EB).copy(alpha = 0.35f)), contentAlignment = Alignment.Center) { Text("Photo 2", fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color(0xFF7C3AED).copy(alpha = 0.35f)), contentAlignment = Alignment.Center) { Text("Photo 3", fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color(0xFF059669).copy(alpha = 0.35f)), contentAlignment = Alignment.Center) { Text("Photo 4", fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                        "3_grid" -> {
                            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.weight(1.4f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Color(0xFFE11D48).copy(alpha = 0.35f)), contentAlignment = Alignment.Center) { Text("Hero 1", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color(0xFF2563EB).copy(alpha = 0.35f)), contentAlignment = Alignment.Center) { Text("Photo 2", fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color(0xFF7C3AED).copy(alpha = 0.35f)), contentAlignment = Alignment.Center) { Text("Photo 3", fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                        "2_split_v" -> {
                            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Color(0xFFE11D48).copy(alpha = 0.35f)), contentAlignment = Alignment.Center) { Text("Left Side", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Color(0xFF2563EB).copy(alpha = 0.35f)), contentAlignment = Alignment.Center) { Text("Right Side", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            }
                        }
                        else -> {
                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color(0xFFE11D48).copy(alpha = 0.35f)), contentAlignment = Alignment.Center) { Text("Top Half", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color(0xFF2563EB).copy(alpha = 0.35f)), contentAlignment = Alignment.Center) { Text("Bottom Half", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Primary CTA button to launch collage studio
                Button(
                    onClick = { onNavigateToService("tool_collage") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_create_photo_collage")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Photo Collage Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ----------------------------------------------------
        // 2. MAIN SERVICES GRID: PHOTO EDITING, PDF TOOLS, BOOKING
        // ----------------------------------------------------
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("main_services_grid_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Section Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Digital Service Hub",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.3).sp
                            )
                        )
                        Text(
                            text = "Photo Editing • PDF Tools • Service Booking",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filterOptions = listOf(
                        "all" to "All (${serviceItems.size})",
                        "photo" to "Photo Editing (5)",
                        "pdf" to "PDF Tools (4)",
                        "booking" to "Booking (4)"
                    )
                    filterOptions.forEach { (catId, label) ->
                        FilterChip(
                            selected = selectedFilter == catId,
                            onClick = { selectedFilter = catId },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = PrimaryBlue
                            ),
                            modifier = Modifier.testTag("dashboard_filter_$catId")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // The Service Grid (2 items per row, responsive FlowRow)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 2
                ) {
                    filteredServices.forEach { item ->
                        DashboardServiceGridCell(
                            item = item,
                            onClick = { onNavigateToService(item.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Service Cell with high-visibility icon, gradient, title, subtitle & badge.
 */
@Composable
fun DashboardServiceGridCell(
    item: DashboardServiceItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .testTag("service_cell_${item.id}")
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vibrant Gradient Icon Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(colors = item.gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.name,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (item.badge != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (item.badge) {
                            "NEW" -> Color(0xFFE11D48)
                            "POPULAR" -> PrimaryBlue
                            "AI" -> PrimaryPurple
                            "FAST" -> SuccessGreen
                            "HD", "-80%" -> Color(0xFF0891B2)
                            else -> Color(0xFF4F46E5)
                        }
                    ) {
                        Text(
                            text = item.badge,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                lineHeight = 13.sp
            )
        }
    }
}
