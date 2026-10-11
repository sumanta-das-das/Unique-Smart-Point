package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.delay

data class BannerSlide(
    val title: String,
    val subtitle: String,
    val drawableRes: Int,
    val targetRoute: String,
    val buttonText: String
)

@Composable
fun HeroSlider(
    onExploreAllTools: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val slides = remember {
        listOf(
            BannerSlide(
                title = "Smart PDF & Document Studio",
                subtitle = "Merge, split, convert to Word, and compress PDFs with zero hassle.",
                drawableRes = R.drawable.banner_pdf,
                targetRoute = "tool_pdf",
                buttonText = "Use PDF Tools"
            ),
            BannerSlide(
                title = "Passport & Studio Photo Maker",
                subtitle = "Create 3.5x4.5cm photos, clean white/blue backgrounds & 4x6 print sheets.",
                drawableRes = R.drawable.banner_passport,
                targetRoute = "tool_passport",
                buttonText = "Make Passport Photo"
            ),
            BannerSlide(
                title = "AI Creative Magic Studio",
                subtitle = "Remove photo backgrounds, restore old portraits, and generate designs.",
                drawableRes = R.drawable.banner_ai,
                targetRoute = "ai_studio",
                buttonText = "Open AI Studio"
            )
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4500)
            currentIndex = (currentIndex + 1) % slides.size
        }
    }

    val currentSlide = slides[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .testTag("hero_banner_card")
                .clickable { onNavigate(currentSlide.targetRoute) }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Banner background image
                Image(
                    painter = painterResource(id = currentSlide.drawableRes),
                    contentDescription = currentSlide.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for typography readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x99000000),
                                    Color(0xDD0B0F19)
                                )
                            )
                        )
                )

                // Slide text and action button
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = currentSlide.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentSlide.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFE2E8F0)
                        ),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { onNavigate(currentSlide.targetRoute) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("hero_action_button")
                        ) {
                            Text(
                                text = currentSlide.buttonText,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Dot indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            slides.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == currentIndex) 20.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentIndex) PrimaryBlue else Color(0xFFCBD5E1)
                        )
                        .clickable { currentIndex = index }
                )
            }
        }

        // Call to action buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onExploreAllTools,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_explore_all_tools"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Explore All Tools", fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(
                onClick = { onNavigate("tool_passport") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_start_creating_free"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Passport Photo", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
