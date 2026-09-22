package pl.pointblank.planszowsky.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    frameColor: Color = RetroBronze,
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

    // 3. Forged frame bars: dark outside, bronze core and a thin worn highlight.
    drawRect(color = borderColor, size = Size(w, h), style = Stroke(width = t * 2f))
    drawRect(color = frameColor, topLeft = Offset(t / 2f, t / 2f), size = Size(w - t, h - t), style = Stroke(width = t))
    drawRect(
        color = RetroBronzeLight.copy(alpha = 0.55f),
        topLeft = Offset(t * 1.25f, t * 1.25f),
        size = Size((w - t * 2.5f).coerceAtLeast(0f), (h - t * 2.5f).coerceAtLeast(0f)),
        style = Stroke(width = (t / 3f).coerceAtLeast(1f))
    )

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
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(RetroObsidian, color, RetroBrown),
            startY = 0f,
            endY = size.height
        )
    )

    // Deterministic stone mottling: texture without bitmap assets or visual noise.
    val tile = 54.dp.toPx()
    val fleck = 2.dp.toPx()
    var row = 0
    var y = 0f
    while (y < size.height) {
        var x = if (row % 2 == 0) tile * 0.2f else tile * 0.65f
        while (x < size.width) {
            drawCircle(
                color = RetroStoneLight.copy(alpha = 0.10f),
                radius = fleck,
                center = Offset(x, y)
            )
            drawLine(
                color = RetroBronzeDark.copy(alpha = 0.10f),
                start = Offset(x - tile * 0.18f, y + tile * 0.12f),
                end = Offset(x + tile * 0.12f, y + tile * 0.22f),
                strokeWidth = 1.dp.toPx()
            )
            x += tile
        }
        row++
        y += tile * 0.72f
    }

    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(RetroOxblood.copy(alpha = 0.30f), Color.Transparent, RetroOxblood.copy(alpha = 0.30f))
        )
    )
}

@Composable
fun RetroPanel(
    modifier: Modifier = Modifier,
    accentColor: Color = RetroBronze,
    backgroundColors: List<Color> = listOf(RetroStone, RetroElementBackground, RetroIron),
    cornerCut: Dp = 10.dp,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = CutCornerShape(cornerCut)
    Box(
        modifier = modifier
            .border(BorderStroke(3.dp, RetroBlack), shape)
            .padding(3.dp)
            .border(BorderStroke(1.dp, accentColor), shape)
            .clip(shape)
            .background(Brush.verticalGradient(backgroundColors))
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun RetroScreenTitle(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    trailing: (@Composable BoxScope.() -> Unit)? = null
) {
    RetroPanel(
        modifier = modifier.fillMaxWidth(),
        accentColor = RetroBronzeLight,
        backgroundColors = listOf(RetroOxblood, RetroRed.copy(alpha = 0.78f), RetroOxblood),
        cornerCut = 12.dp,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
    ) {
        if (onBackClick != null) {
            RetroOrbButton(
                onClick = onBackClick,
                color = RetroStone,
                buttonSize = 44.dp,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Box(modifier = Modifier.size(24.dp)) {
                    PixelBackIcon(color = RetroText)
                }
            }
        }

        Text(
            text = title.uppercase(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(
                    horizontal = when {
                        trailing != null -> 88.dp
                        onBackClick != null -> 52.dp
                        else -> 12.dp
                    }
                ),
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                letterSpacing = 0.8.sp,
                color = RetroText
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (trailing != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd), content = trailing)
        }
    }
}

@Composable
fun RetroMedallion(
    color: Color,
    icon: @Composable () -> Unit,
    buttonSize: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(buttonSize),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            drawCircle(RetroBlack, radius = radius)
            drawCircle(RetroBronzeDark, radius = radius * 0.88f)
            drawCircle(RetroBronzeLight, radius = radius * 0.76f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.78f),
                        color,
                        RetroBlack.copy(alpha = 0.92f)
                    ),
                    center = Offset(size.width * 0.38f, size.height * 0.32f),
                    radius = radius * 1.15f
                ),
                radius = radius * 0.69f
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.18f),
                radius = radius * 0.17f,
                center = Offset(size.width * 0.34f, size.height * 0.28f)
            )
        }
        icon()
    }
}

@Composable
fun RetroOrbButton(
    onClick: () -> Unit,
    color: Color,
    buttonSize: Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit
) {
    RetroMedallion(
        color = color,
        icon = icon,
        buttonSize = buttonSize,
        modifier = modifier.clip(CircleShape).clickable(enabled = enabled, onClick = onClick)
    )
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
    RetroPanel(
        modifier = modifier,
        accentColor = if (accentColor == Color.Transparent) RetroBronze else accentColor,
        backgroundColors = listOf(
            backgroundColor.copy(alpha = 0.96f),
            backgroundColor,
            RetroIron
        ),
        cornerCut = thickness * 4,
        contentPadding = PaddingValues(thickness * 3),
        content = content
    )
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
    val shape = CutCornerShape(7.dp)
    Box(
        modifier = modifier
            .size(44.dp)
            .border(3.dp, RetroBlack, shape)
            .padding(3.dp)
            .border(1.dp, RetroBronze, shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        if (enabled) color.copy(alpha = 0.82f) else RetroGrey,
                        if (enabled) color else RetroGrey.copy(alpha = 0.7f),
                        RetroIron
                    )
                )
            )
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
    val shape = CutCornerShape(9.dp)
    val accent = if (color == RetroGold || color == RetroEmber) RetroBronzeLight else RetroBronze
    Box(
        modifier = modifier
            .heightIn(min = 56.dp)
            .border(3.dp, RetroBlack, shape)
            .padding(3.dp)
            .border(1.dp, accent, shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.82f), color, RetroIron)))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = if (color == RetroGold || color == RetroEmber) RetroBlack else RetroText
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
    val shape = CutCornerShape(6.dp)
    Box(
        modifier = modifier
            .heightIn(min = 38.dp)
            .clickable(onClick = onClick)
            .border(2.dp, RetroBlack, shape)
            .padding(2.dp)
            .border(1.dp, if (isSelected) RetroBronzeLight else RetroBronzeDark, shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    if (isSelected) listOf(RetroEmber, RetroGold, RetroBronze)
                    else listOf(RetroStone, RetroElementBackground, RetroIron)
                )
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif,
                color = if (isSelected) RetroBlack else RetroText,
                fontSize = 11.sp
            )
        )
    }
}
