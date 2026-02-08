package pl.pointblank.planszowsky.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

fun Modifier.retroCrtEffect() = this // Temporarily disabled for performance

fun Modifier.pixelFrame(
    borderColor: Color = RetroBlack,
    innerColor: Color = Color.Transparent,
    thickness: Dp = 2.dp
) = this.drawWithContent {
    val t = thickness.toPx()
    val w = size.width
    val h = size.height

    // 1. Shadow (Outer offset - drawn first)
    drawRect(
        color = Color.Black.copy(alpha = 0.4f),
        topLeft = Offset(t, t),
        size = Size(w, h)
    )

    // 2. Main Outer Black Frame
    drawRect(color = RetroBlack, size = Size(w, h))

    // 3. Highlight Bevel (Top & Left) - Silver/Stone Light
    val highlightColor = Color(0xFFC0C0C0) 
    drawRect(
        color = highlightColor,
        topLeft = Offset(t, t),
        size = Size(w - 2 * t, t)
    )
    drawRect(
        color = highlightColor,
        topLeft = Offset(t, t),
        size = Size(t, h - 2 * t)
    )

    // 4. Lowlight Bevel (Bottom & Right) - Deep Charcoal
    val lowlightColor = Color(0xFF404040)
    drawRect(
        color = lowlightColor,
        topLeft = Offset(t, h - 2 * t),
        size = Size(w - 2 * t, t)
    )
    drawRect(
        color = lowlightColor,
        topLeft = Offset(w - 2 * t, t),
        size = Size(t, h - 2 * t)
    )

    // 5. Inner Inset (Thin black line before content)
    drawRect(
        color = RetroBlack,
        topLeft = Offset(2 * t, 2 * t),
        size = Size(w - 4 * t, h - 4 * t),
        style = Stroke(width = 1f)
    )

    drawContent()

    // 6. Optional Accent (Gold/Cyan) - Drawn ON TOP of content if needed
    if (innerColor != Color.Transparent) {
        drawRect(
            color = innerColor,
            topLeft = Offset(2 * t, 2 * t),
            size = Size(w - 4 * t, h - 4 * t),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

fun Modifier.pixelButtonFrame(
    isSelected: Boolean = false,
    thickness: Dp = 1.dp
) = this.drawWithContent {
    val t = thickness.toPx()
    val w = size.width
    val h = size.height

    // 1. Clean Black Outer Border (No heavy shadow)
    drawRect(color = RetroBlack, size = Size(w, h))

    // 2. Simple Pixel Bevel
    val light = if (isSelected) RetroGold else Color.White.copy(alpha = 0.3f)
    val dark = if (isSelected) Color.Black.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.2f)

    if (!isSelected) {
        // Highlight
        drawRect(color = light, topLeft = Offset(t, t), size = Size(w - 2 * t, t))
        drawRect(color = light, topLeft = Offset(t, t), size = Size(t, h - 2 * t))
        // Shadow
        drawRect(color = dark, topLeft = Offset(t, h - 2 * t), size = Size(w - 2 * t, t))
        drawRect(color = dark, topLeft = Offset(w - 2 * t, t), size = Size(t, h - 2 * t))
    } else {
        // Pressed
        drawRect(color = dark, topLeft = Offset(t, t), size = Size(w - 2 * t, t))
        drawRect(color = dark, topLeft = Offset(t, t), size = Size(t, h - 2 * t))
    }

    drawContent()
}

fun Modifier.rpgGameFrame(
    frameColor: Color = RetroGold,
    borderColor: Color = RetroBlack,
    thickness: Dp = 4.dp
) = this.drawWithContent {
    val t = thickness.toPx()
    val w = size.width
    val h = size.height
    
    // 1. Draw the actual image/content
    drawContent()

    // 2. Inner Shadow (making the image look recessed)
    drawRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset(0f, 0f),
        size = Size(w, t * 1.5f)
    )
    drawRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset(0f, 0f),
        size = Size(t * 1.5f, h)
    )

    // 3. Main Frame Bars
    drawRect(color = frameColor, topLeft = Offset(0f, 0f), size = Size(w, t)) // Top
    drawRect(color = frameColor, topLeft = Offset(0f, h - t), size = Size(w, t)) // Bottom
    drawRect(color = frameColor, topLeft = Offset(0f, 0f), size = Size(t, h)) // Left
    drawRect(color = frameColor, topLeft = Offset(w - t, 0f), size = Size(t, h)) // Right

    // 4. Decorative Corners
    val cornerSize = t * 2.5f
    fun drawCorner(offsetX: Float, offsetY: Float) {
        // Outer box of the corner
        drawRect(color = borderColor, topLeft = Offset(offsetX, offsetY), size = Size(cornerSize, cornerSize))
        // Inner gold fill
        drawRect(
            color = frameColor, 
            topLeft = Offset(offsetX + t/2, offsetY + t/2), 
            size = Size(cornerSize - t, cornerSize - t)
        )
        // Highlight on the corner
        drawRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(offsetX + t/2, offsetY + t/2),
            size = Size(t, t)
        )
    }

    drawCorner(0f, 0f)
    drawCorner(w - cornerSize, 0f)
    drawCorner(0f, h - cornerSize)
    drawCorner(w - cornerSize, h - cornerSize)

    // 5. Outer Black Border
    drawRect(
        color = borderColor,
        size = Size(w, h),
        style = Stroke(width = 1.dp.toPx())
    )
}

@Composable
fun RetroGameCover(
    imageUrl: String,
    modifier: Modifier = Modifier,
    isWishlisted: Boolean = false
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.None,
        modifier = modifier
            .rpgGameFrame(
                frameColor = if (isWishlisted) RetroGold else RetroElementBackground,
                thickness = 4.dp
            )
    )
}

fun DrawScope.drawDitheredOverlay(alpha: Float = 0.15f) {
    // Disabled heavy loop
}

fun Modifier.retroBackground(color: Color = RetroBackground) = this.drawBehind {
    // Fill main background
    drawRect(color)
}

@Composable
fun RetroChunkyBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = RetroElementBackground,
    borderColor: Color = RetroBlack,
    accentColor: Color = Color.Transparent,
    thickness: Dp = 2.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .pixelFrame(borderColor = borderColor, innerColor = accentColor, thickness = thickness)
            .background(backgroundColor)
            .padding(thickness * 3) // Content inset to stay inside the bevel
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
            .pixelButtonFrame(thickness = 3.dp)
            .background(color)
            .clickable(onClick = onClick),
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
            .pixelFrame(thickness = 2.dp)
            .background(color)
            .clickable(onClick = onClick),
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
    // Optimization: Skip heavy drawing
}

fun DrawScope.drawDitheredShadow(size: Size) {
    // Optimization: Use a simple solid shadow for now to keep it smooth
    val shadowOffset = 4.dp.toPx()
    drawRect(
        color = Color.Black.copy(alpha = 0.3f),
        topLeft = Offset(shadowOffset, shadowOffset),
        size = size
    )
}
