package com.planszowsky.android.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.retroBackground(color: Color = RetroBackground) = this.drawBehind {
    // Fill main background
    drawRect(color)
    
    // Add subtle dithering texture
    val dotSize = 2.dp.toPx()
    for (y in 0 until (size.height / dotSize).toInt()) {
        for (x in 0 until (size.width / dotSize).toInt()) {
            if ((x + y) % 8 == 0) { // Subtle 1/8th pattern
                drawRect(
                    color = Color.Black.copy(alpha = 0.1f),
                    topLeft = Offset(x * dotSize, y * dotSize),
                    size = Size(dotSize, dotSize)
                )
            }
        }
    }
}

@Composable
fun RetroChunkyBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = RetroElementBackground,
    borderColor: Color = RetroGold,
    showShadow: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .then(if (showShadow) Modifier.drawBehind { drawDitheredShadow(size) } else Modifier)
            .background(backgroundColor)
            .drawBehind {
                val stroke = 3.dp.toPx()
                // Outer Black Border
                drawRect(RetroBlack, style = Stroke(stroke * 2f))
                // Inner Accent Border (Gold/Brass)
                if (borderColor != Color.Transparent) {
                    drawRect(borderColor, style = Stroke(stroke), topLeft = Offset(stroke/2, stroke/2), size = Size(size.width - stroke, size.height - stroke))
                }
            }
    ) {
        content()
    }
}

@Composable
fun RetroFloatingButton(
    onClick: () -> Unit, 
    color: Color, 
    icon: @Composable () -> Unit, 
    buttonSize: Dp
) {
    Box(
        modifier = Modifier
            .size(buttonSize)
            .drawBehind { drawDitheredShadow(size) }
            .background(color)
            .clickable(onClick = onClick)
            .drawBehind {
                val stroke = 4.dp.toPx()
                drawRect(RetroBlack, style = Stroke(stroke))
                // Inner highlight
                drawRect(Color.White.copy(alpha = 0.2f), Offset(stroke, stroke), Size(size.width - stroke*2, stroke))
            },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
fun RetroSquareIconButton(
    onClick: () -> Unit, 
    color: Color, 
    icon: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(color)
            .clickable(onClick = onClick)
            .drawBehind {
                val stroke = 3.dp.toPx()
                drawRect(RetroBlack, style = Stroke(stroke))
            },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
fun RetroSquareButton(
    text: String, 
    color: Color, 
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .background(color)
            .clickable(onClick = onClick)
            .drawBehind {
                val stroke = 3.dp.toPx()
                drawRect(RetroBlack, style = Stroke(stroke))
            }
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = RetroText
            )
        )
    }
}

fun DrawScope.drawDitheringPattern(color: Color) {
    val dotSize = 2.dp.toPx()
    for (y in 0 until (size.height / dotSize).toInt()) {
        for (x in 0 until (size.width / dotSize).toInt()) {
            if ((x + y) % 2 == 0) {
                drawRect(
                    color = color,
                    topLeft = Offset(x * dotSize, y * dotSize),
                    size = Size(dotSize, dotSize)
                )
            }
        }
    }
}

fun DrawScope.drawDitheredShadow(size: Size) {
    val shadowOffset = 6.dp.toPx()
    val dotSize = 2.dp.toPx()
    for (y in 0 until ((size.height + shadowOffset) / dotSize).toInt()) {
        for (x in 0 until ((size.width + shadowOffset) / dotSize).toInt()) {
            val inMainX = x * dotSize < size.width
            val inMainY = y * dotSize < size.height
            
            if ((!inMainX || !inMainY) && (x + y) % 2 == 0) {
                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    topLeft = Offset(x * dotSize, y * dotSize),
                    size = Size(dotSize, dotSize)
                )
            }
        }
    }
}