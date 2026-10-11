package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Brand Palette: Blue, Purple, Cyan
val PrimaryBlue = Color(0xFF1E40AF)
val PrimaryBlueLight = Color(0xFF3B82F6)
val PrimaryPurple = Color(0xFF7C3AED)
val PrimaryPurpleLight = Color(0xFF8B5CF6)
val AccentCyan = Color(0xFF06B6D4)
val AccentCyanLight = Color(0xFF22D3EE)

// Light Theme Colors
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightBorder = Color(0xFFE2E8F0)

// Dark Theme Colors
val DarkBackground = Color(0xFF0B0F19)
val DarkSurface = Color(0xFF131B2E)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkBorder = Color(0xFF334155)

// Status & Accents
val SuccessGreen = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFEF4444)

// Gradient Brushes
val BrandGradient = Brush.linearGradient(
    colors = listOf(PrimaryBlue, PrimaryPurple, AccentCyan)
)

val HeroCardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF1E3A8A), Color(0xFF4C1D95), Color(0xFF0E7490))
)

val AccentGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryPurpleLight, AccentCyanLight)
)
