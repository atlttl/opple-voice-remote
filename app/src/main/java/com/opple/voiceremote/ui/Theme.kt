package com.opple.voiceremote.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class Palette(
    val canvas: Color,
    val canvasGlow: Color,
    val body: Color,
    val bodyHigh: Color,
    val hairline: Color,
    val hairlineStrong: Color,
    val text: Color,
    val textDim: Color,
    val textFaint: Color,
    val accent: Color,
    val accentSoft: Color,
    val brand: Color,
    val danger: Color,
)

val DarkPalette = Palette(
    canvas = Color(0xFF14120F),
    canvasGlow = Color(0xFF2A2118),
    body = Color(0xFF211E19),
    bodyHigh = Color(0xFF2A261F),
    hairline = Color(0xFF3A342A),
    hairlineStrong = Color(0xFF544C3D),
    text = Color(0xFFEDE7DA),
    textDim = Color(0xFFA79F90),
    textFaint = Color(0xFF6E675B),
    accent = Color(0xFFD9A05F),
    accentSoft = Color(0x2ED9A05F),
    brand = Color(0xFF8CA6D6),
    danger = Color(0xFFD08A7A),
)

val LightPalette = Palette(
    canvas = Color(0xFFF1EDE4),
    canvasGlow = Color(0xFFEDE4D3),
    body = Color(0xFFFBF8F1),
    bodyHigh = Color(0xFFFFFFFF),
    hairline = Color(0xFFDDD5C7),
    hairlineStrong = Color(0xFFC0B6A3),
    text = Color(0xFF1C1915),
    textDim = Color(0xFF6A6357),
    textFaint = Color(0xFF9A9384),
    accent = Color(0xFFB06E33),
    accentSoft = Color(0x1FB06E33),
    brand = Color(0xFF29477F),
    danger = Color(0xFF9B4A38),
)

val LocalPalette = staticCompositionLocalOf { DarkPalette }

// 全局不使用粗体：层级只由字号、字距、颜色与留白承担
private val AppTypography = Typography(
    displaySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 27.sp, lineHeight = 34.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 29.sp, letterSpacing = (-0.2).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.5.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.3.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 0.6.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 9.5.sp, lineHeight = 13.sp, letterSpacing = 1.1.sp),
)

@Composable
fun OppleTheme(dark: Boolean, content: @Composable () -> Unit) {
    val p = if (dark) DarkPalette else LightPalette
    val scheme = if (dark) {
        darkColorScheme(
            primary = p.accent,
            onPrimary = Color(0xFF1B1610),
            secondary = p.brand,
            background = p.canvas,
            onBackground = p.text,
            surface = p.body,
            onSurface = p.text,
            surfaceVariant = p.bodyHigh,
            onSurfaceVariant = p.textDim,
            outline = p.hairline,
            error = p.danger,
        )
    } else {
        lightColorScheme(
            primary = p.accent,
            onPrimary = Color(0xFFFFFBF4),
            secondary = p.brand,
            background = p.canvas,
            onBackground = p.text,
            surface = p.body,
            onSurface = p.text,
            surfaceVariant = p.bodyHigh,
            onSurfaceVariant = p.textDim,
            outline = p.hairline,
            error = p.danger,
        )
    }
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
    }
}
