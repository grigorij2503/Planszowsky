package com.planszowsky.android.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.planszowsky.android.domain.model.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    secondary = SecondaryNeon,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val PixelColorScheme = darkColorScheme(
    primary = PixelPrimary,
    secondary = PixelSecondary,
    tertiary = PixelTertiary,
    background = PixelBackground,
    surface = PixelSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = PixelText,
    onSurface = PixelText
)

@Composable
fun PlanszowskyTheme(
    appTheme: AppTheme = AppTheme.MODERN,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.MODERN -> DarkColorScheme
        AppTheme.PIXEL_ART -> PixelColorScheme
    }
    
    val typography = when (appTheme) {
        AppTheme.MODERN -> Typography
        AppTheme.PIXEL_ART -> PixelTypography
    }

    val shapes = if (appTheme == AppTheme.PIXEL_ART) {
        val square = RoundedCornerShape(0.dp)
        Shapes(
            extraSmall = square,
            small = square,
            medium = square,
            large = square,
            extraLarge = square
        )
    } else {
        MaterialTheme.shapes
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            // Ustawiamy ikony na jasne, ponieważ mamy ciemne tło (Dark Mode)
            windowInsetsController.isAppearanceLightStatusBars = false
            windowInsetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
