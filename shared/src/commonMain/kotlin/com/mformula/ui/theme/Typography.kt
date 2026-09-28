package com.mformula.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun mformulaTypography(): Typography {
    val displayFont = FontFamily.SansSerif
    val monoFont = FontFamily.Monospace

    return Typography(
        headlineLarge = TextStyle(
            fontFamily = displayFont,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            letterSpacing = (-0.5).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = displayFont,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge = TextStyle(
            fontFamily = monoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            letterSpacing = 1.sp
        ),
        labelLarge = TextStyle(
            fontFamily = monoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = monoFont,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        )
    )
}

private val MformulaColorScheme = darkColorScheme(
    background = Color(0xFF101014),
    surface = Color(0xFF1A1A22),
    primary = Color(0xFFF24E1E),
    secondary = Color(0xFFFFCC00),
    tertiary = Color(0xFF00E599),
    onBackground = Color(0xFFFDFBF7),
    onSurface = Color(0xFFFDFBF7)
)

@Composable
fun MformulaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MformulaColorScheme,
        typography = mformulaTypography(),
        content = content
    )
}
