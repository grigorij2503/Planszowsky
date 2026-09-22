package pl.pointblank.planszowsky.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun Modifier.pixelFrame(
    borderColor: Color = RetroIron,
    innerColor: Color = Color.Transparent,
    thickness: Dp = 2.dp
) = this.drawWithContent {
    val t = thickness.toPx()
    val w = size.width
    val h = size.height

    drawContent()

    val borderWidth = t * 2f
    drawRect(color = borderColor, size = Size(w, borderWidth))
    drawRect(color = borderColor, topLeft = Offset(0f, h - borderWidth), size = Size(w, borderWidth))
    drawRect(color = borderColor, size = Size(borderWidth, h))
    drawRect(color = borderColor, topLeft = Offset(w - borderWidth, 0f), size = Size(borderWidth, h))

    val highlightColor = RetroBronzeLight
    drawRect(
        color = highlightColor,
        topLeft = Offset(borderWidth, borderWidth),
        size = Size((w - borderWidth * 2).coerceAtLeast(0f), t)
    )
    drawRect(
        color = highlightColor,
        topLeft = Offset(borderWidth, borderWidth),
        size = Size(t, (h - borderWidth * 2).coerceAtLeast(0f))
    )

    val lowlightColor = RetroBrown
    drawRect(
        color = lowlightColor,
        topLeft = Offset(borderWidth, h - borderWidth - t),
        size = Size((w - borderWidth * 2).coerceAtLeast(0f), t)
    )
    drawRect(
        color = lowlightColor,
        topLeft = Offset(w - borderWidth - t, borderWidth),
        size = Size(t, (h - borderWidth * 2).coerceAtLeast(0f))
    )

    val corner = borderWidth * 1.5f
    listOf(
        Offset(0f, 0f),
        Offset(w - corner, 0f),
        Offset(0f, h - corner),
        Offset(w - corner, h - corner)
    ).forEach { offset ->
        drawRect(color = RetroBlack, topLeft = offset, size = Size(corner, corner))
        drawRect(
            color = RetroBronze,
            topLeft = offset + Offset(t / 2f, t / 2f),
            size = Size((corner - t).coerceAtLeast(0f), (corner - t).coerceAtLeast(0f))
        )
    }

    if (innerColor != Color.Transparent) {
        drawRect(
            color = innerColor,
            topLeft = Offset(borderWidth, borderWidth),
            size = Size(
                (w - borderWidth * 2).coerceAtLeast(0f),
                (h - borderWidth * 2).coerceAtLeast(0f)
            ),
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

    drawContent()

    drawRect(color = RetroIron, size = Size(w, h), style = Stroke(width = t * 2f))

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

fun Modifier.retroBackground(color: Color = RetroBackground) = this.drawBehind {
    drawRect(color)

    val grainStep = 18.dp.toPx()
    val grainSize = 1.dp.toPx()
    var row = 0
    var y = 0f
    while (y < size.height) {
        var x = if (row % 2 == 0) 0f else grainStep / 2f
        while (x < size.width) {
            drawRect(
                color = RetroLeatherGrain.copy(alpha = 0.28f),
                topLeft = Offset(x, y),
                size = Size(grainSize, grainSize)
            )
            x += grainStep
        }
        row++
        y += grainStep
    }
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
    buttonSize: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape
) {
    Box(
        modifier = modifier
            .size(buttonSize)
            .pixelButtonFrame(thickness = 3.dp)
            .clip(shape)
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
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .pixelFrame(thickness = 2.dp)
            .background(if (enabled) color else Color.Gray)
            .clickable(enabled = enabled, onClick = onClick),
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
            .heightIn(min = 56.dp)
            .pixelButtonFrame(thickness = 2.dp)
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = if (color == RetroGold) RetroBlack else Color.White
            )
        )
    }
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

@Composable
fun RetroFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 38.dp)
            .clickable(onClick = onClick)
            .pixelButtonFrame(isSelected = isSelected, thickness = 2.dp)
            .padding(2.dp)
            .background(if (isSelected) RetroGold else RetroElementBackground)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) RetroBlack else RetroText,
                fontSize = 11.sp
            )
        )
    }
}
