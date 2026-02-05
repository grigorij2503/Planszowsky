package com.planszowsky.android.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.planszowsky.android.ui.viewmodel.DiceMode
import com.planszowsky.android.ui.viewmodel.DiceUiState
import com.planszowsky.android.ui.viewmodel.DiceViewModel
import com.planszowsky.android.ui.viewmodel.DieState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DiceScreen(
    viewModel: DiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState.isRolling) {
        if (uiState.isRolling) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Suma: ${if (uiState.isRolling) "..." else uiState.totalSum}",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            DiceArena(
                dice = uiState.dice,
                isRolling = uiState.isRolling
            )
        }

        DiceControls(
            uiState = uiState,
            onModeSelect = viewModel::onModeSelected,
            onCustomConfigChange = viewModel::onCustomDiceConfigChanged,
            onRoll = viewModel::rollDice
        )
    }
}

@Composable
fun DiceArena(dice: List<DieState>, isRolling: Boolean) {
    val columns = when {
        dice.size == 1 -> 1
        dice.size <= 4 -> 2
        dice.size <= 9 -> 3
        else -> 4
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(dice, key = { it.id }) { die ->
            AnimatedDie(die = die, isRolling = isRolling)
        }
    }
}

@Composable
fun AnimatedDie(die: DieState, isRolling: Boolean) {
    val shakeAnim = remember { Animatable(0f) }
    
    LaunchedEffect(isRolling) {
        if (isRolling) {
            while (true) {
                shakeAnim.animateTo(
                    targetValue = 10f,
                    animationSpec = tween(50, easing = LinearEasing)
                )
                shakeAnim.animateTo(
                    targetValue = -10f,
                    animationSpec = tween(50, easing = LinearEasing)
                )
            }
        } else {
            shakeAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isRolling) 0.9f else 1f,
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .rotate(die.rotation + shakeAnim.value)
    ) {
        if (die.sides == 6) {
            DieD6Visual(value = die.value)
        } else {
            DieGenericVisual(value = die.value, sides = die.sides)
        }
    }
}

@Composable
fun DieD6Visual(value: Int) {
    val dieColor = MaterialTheme.colorScheme.primaryContainer
    val pipColor = MaterialTheme.colorScheme.onPrimaryContainer

    Canvas(modifier = Modifier.fillMaxSize(0.8f)) {
        drawRoundRect(
            color = dieColor,
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            size = size
        )

        val r = size.width / 10f
        val w = size.width
        val h = size.height

        fun drawPip(x: Float, y: Float) {
            drawCircle(color = pipColor, radius = r, center = Offset(x, y))
        }

        if (value % 2 != 0) drawPip(w / 2, h / 2)
        if (value > 1) {
            drawPip(w * 0.25f, h * 0.25f)
            drawPip(w * 0.75f, h * 0.75f)
        }
        if (value > 3) {
            drawPip(w * 0.75f, h * 0.25f)
            drawPip(w * 0.25f, h * 0.75f)
        }
        if (value == 6) {
            drawPip(w * 0.25f, h * 0.5f)
            drawPip(w * 0.75f, h * 0.5f)
        }
    }
}

@Composable
fun DieGenericVisual(value: Int, sides: Int) {
    val dieColor = MaterialTheme.colorScheme.tertiaryContainer
    val textColor = MaterialTheme.colorScheme.onTertiaryContainer
    val borderColor = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = Modifier.fillMaxSize(0.85f)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.width / 2

        val path = Path()
        
        when (sides) {
            4 -> {
                path.moveTo(centerX, 0f)
                path.lineTo(size.width, size.height * 0.85f)
                path.lineTo(0f, size.height * 0.85f)
                path.close()
            }
            12, 20 -> {
                val steps = 6
                for (i in 0 until steps) {
                    val theta = (i * 2 * Math.PI / steps) - Math.PI / 2
                    val x = centerX + radius * cos(theta).toFloat()
                    val y = centerY + radius * sin(theta).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
            }
            else -> {
                val steps = 8
                for (i in 0 until steps) {
                    val theta = (i * 2 * Math.PI / steps) - Math.PI / 8
                    val x = centerX + radius * cos(theta).toFloat()
                    val y = centerY + radius * sin(theta).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
            }
        }

        drawPath(path = path, color = dieColor)
        drawPath(path = path, color = borderColor, style = Stroke(width = 3.dp.toPx()))

        drawContext.canvas.nativeCanvas.apply {
            val paint = Paint().apply {
                color = textColor.toArgb()
                textSize = radius * 0.8f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            val textHeight = paint.descent() - paint.ascent()
            val textOffset = (textHeight / 2) - paint.descent()
            
            val yPos = if (sides == 4) centerY + textOffset + (radius * 0.2f) else centerY + textOffset

            drawText(value.toString(), centerX, yPos, paint)
        }
        
        drawContext.canvas.nativeCanvas.apply {
             val paint = Paint().apply {
                color = textColor.copy(alpha = 0.7f).toArgb()
                textSize = radius * 0.25f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT
            }
            val yPos = if (sides == 4) size.height * 0.75f else size.height * 0.85f
            drawText("k$sides", centerX, yPos, paint)
        }
    }
}

@Composable
fun DiceControls(
    uiState: DiceUiState,
    onModeSelect: (DiceMode) -> Unit,
    onCustomConfigChange: (Int, Int) -> Unit,
    onRoll: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModeChip(
                    text = "1k6",
                    selected = uiState.mode == DiceMode.ONE_D6,
                    onClick = { onModeSelect(DiceMode.ONE_D6) }
                )
                ModeChip(
                    text = "2k6",
                    selected = uiState.mode == DiceMode.TWO_D6,
                    onClick = { onModeSelect(DiceMode.TWO_D6) }
                )
                ModeChip(
                    text = "Inne",
                    selected = uiState.mode == DiceMode.CUSTOM,
                    onClick = { onModeSelect(DiceMode.CUSTOM) }
                )
            }

            if (uiState.mode == DiceMode.CUSTOM) {
                Spacer(modifier = Modifier.height(16.dp))
                CustomDiceSettings(
                    count = uiState.customDiceCount,
                    sides = uiState.customDiceSides,
                    onConfigChange = onCustomConfigChange
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRoll,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isRolling
            ) {
                Icon(Icons.Default.Casino, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (uiState.isRolling) "Turlam..." else "RZUĆ!", fontSize = 18.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, textAlign = TextAlign.Center, modifier = Modifier.defaultMinSize(minWidth = 40.dp)) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun CustomDiceSettings(count: Int, sides: Int, onConfigChange: (Int, Int) -> Unit) {
    Column {
        Text("Liczba kości: $count", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = count.toFloat(),
            onValueChange = { onConfigChange(it.toInt(), sides) },
            valueRange = 1f..10f,
            steps = 8
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Rodzaj kości: k$sides", style = MaterialTheme.typography.labelLarge)
        val commonSides = listOf(4, 6, 8, 10, 12, 20, 100)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            commonSides.take(5).forEach { s ->
                SuggestionChip(
                    onClick = { onConfigChange(count, s) },
                    label = { Text("k$s") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (sides == s) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                    )
                )
            }
        }
        if (sides !in commonSides.take(5)) {
             Slider(
                value = sides.toFloat(),
                onValueChange = { onConfigChange(count, it.toInt()) },
                valueRange = 2f..100f
            )
        }
    }
}
