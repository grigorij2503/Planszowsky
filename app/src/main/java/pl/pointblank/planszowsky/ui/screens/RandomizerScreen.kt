package pl.pointblank.planszowsky.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pl.pointblank.planszowsky.R
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.ui.theme.*
import pl.pointblank.planszowsky.ui.viewmodel.RandomizerViewModel
import pl.pointblank.planszowsky.ui.viewmodel.DurationFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomizerScreen(
    viewModel: RandomizerViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val selectedGame by viewModel.selectedGame.collectAsState()
    val isSpinning by viewModel.isSpinning.collectAsState()
    val games by viewModel.games.collectAsState()
    val filteredGames by viewModel.filteredGames.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val playerFilter by viewModel.playerFilter.collectAsState()
    val durationFilter by viewModel.durationFilter.collectAsState()
    val isRetro = appTheme == AppTheme.PIXEL_ART
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.then(if (isRetro) Modifier.retroBackground() else Modifier),
        containerColor = if (isRetro) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            if (isRetro) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RetroSquareIconButton(onClick = onBackClick, color = RetroElementBackground) {
                        PixelBackIcon(color = RetroText)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.randomizer_title).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, color = RetroText)
                    )
                }
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.randomizer_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button))
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filters Section
            RandomizerFilters(
                isRetro = isRetro,
                playerFilter = playerFilter,
                durationFilter = durationFilter,
                onPlayerChange = viewModel::setPlayerFilter,
                onDurationChange = viewModel::setDurationFilter
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Slot Machine Area
            if (isRetro) {
                RetroSlotMachine(
                    isSpinning = isSpinning,
                    selectedGame = selectedGame,
                    allGames = filteredGames
                )
            } else {
                ModernSlotMachine(
                    isSpinning = isSpinning,
                    selectedGame = selectedGame
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isRetro) {
                RetroSquareButton(
                    text = if (isSpinning) stringResource(R.string.spinning).uppercase() else stringResource(R.string.spin_button).uppercase(),
                    color = if (isSpinning || filteredGames.isEmpty()) RetroGrey else RetroGreen,
                    modifier = Modifier.fillMaxWidth(0.7f).padding(bottom = 16.dp),
                    onClick = { if (filteredGames.isNotEmpty()) viewModel.spin() }
                )
            } else {
                Button(
                    onClick = { viewModel.spin() },
                    enabled = !isSpinning && filteredGames.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(64.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isSpinning) stringResource(R.string.spinning) else stringResource(R.string.spin_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (games.isEmpty()) {
                Text(
                    text = stringResource(R.string.empty_collection_warning).let { if(isRetro) it.uppercase() else it },
                    modifier = Modifier.padding(bottom = 32.dp),
                    style = if (isRetro) MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = RetroRed)
                            else MaterialTheme.typography.bodySmall,
                    color = if (isRetro) RetroRed else MaterialTheme.colorScheme.error
                )
            } else if (filteredGames.isEmpty() && !isSpinning) {
                Text(
                    text = stringResource(R.string.no_games_matching).let { if(isRetro) it.uppercase() else it },
                    modifier = Modifier.padding(bottom = 32.dp),
                    style = if (isRetro) MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = RetroOrange)
                            else MaterialTheme.typography.bodySmall,
                    color = if (isRetro) RetroOrange else MaterialTheme.colorScheme.tertiary
                )
            } else {
                // Safe area spacer
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun RandomizerFilters(
    isRetro: Boolean,
    playerFilter: Int?,
    durationFilter: DurationFilter,
    onPlayerChange: (Int?) -> Unit,
    onDurationChange: (DurationFilter) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Player Count Filter
        Text(
            text = stringResource(R.string.filter_players_label).let { if (isRetro) it.uppercase() else it },
            style = if (isRetro) MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace, color = RetroText)
                    else MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items((1..6).toList()) { count ->
                val isSelected = playerFilter == count
                if (isRetro) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isSelected) RetroGold else RetroElementBackground)
                            .clickable { onPlayerChange(if (isSelected) null else count) }
                            .drawBehind {
                                drawRect(RetroBlack, style = Stroke(2.dp.toPx()))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) RetroBlack else RetroText
                            )
                        )
                    }
                } else {
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPlayerChange(if (isSelected) null else count) },
                        label = { Text(count.toString()) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Duration Filter
        Text(
            text = stringResource(R.string.filter_duration_label).let { if (isRetro) it.uppercase() else it },
            style = if (isRetro) MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace, color = RetroText)
                    else MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(DurationFilter.entries) { filter ->
                val isSelected = durationFilter == filter
                val label = when(filter) {
                    DurationFilter.ANY -> stringResource(R.string.duration_any)
                    DurationFilter.SHORT -> stringResource(R.string.duration_short)
                    DurationFilter.MEDIUM -> stringResource(R.string.duration_medium)
                    DurationFilter.LONG -> stringResource(R.string.duration_long)
                }
                
                if (isRetro) {
                    RetroFilterChip(
                        text = label.uppercase(),
                        isSelected = isSelected,
                        onClick = { onDurationChange(filter) }
                    )
                } else {
                    FilterChip(
                        selected = isSelected,
                        onClick = { onDurationChange(filter) },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernSlotMachine(isSpinning: Boolean, selectedGame: pl.pointblank.planszowsky.domain.model.Game?) {
    Box(
        modifier = Modifier
            .size(280.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSpinning) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else if (selectedGame != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = selectedGame.imageUrl ?: selectedGame.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = selectedGame.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } else {
            Text(text = "🎲", fontSize = 64.sp)
        }
    }
}

@Composable
fun RetroSlotMachine(
    isSpinning: Boolean,
    selectedGame: pl.pointblank.planszowsky.domain.model.Game?,
    allGames: List<pl.pointblank.planszowsky.domain.model.Game>
) {
    var displayGame by remember { mutableStateOf(selectedGame) }
    
    // Shuffle animation for retro look
    if (isSpinning) {
        LaunchedEffect(allGames) {
            while(true) {
                if (allGames.isNotEmpty()) {
                    displayGame = allGames.random()
                }
                kotlinx.coroutines.delay(100)
            }
        }
    } else {
        displayGame = selectedGame
    }

    RetroChunkyBox(
        modifier = Modifier
            .size(300.dp)
            .padding(4.dp),
        backgroundColor = RetroBlack,
        accentColor = if (isSpinning) RetroGold else RetroBlue
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (displayGame != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(displayGame?.imageUrl ?: displayGame?.thumbnailUrl)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RectangleShape),
                        contentScale = ContentScale.Fit,
                        filterQuality = FilterQuality.None
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = displayGame?.title?.uppercase() ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = RetroText
                        ),
                        maxLines = 2,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                // Retro Dice placeholder
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(64.dp)) {
                        PixelDiceIcon(false)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.ready_label),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            color = RetroGold
                        )
                    )
                }
            }
            
            // Decorative scanlines just for the slot display
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scanlineWidth = 2.dp.toPx()
                for (i in 0 until (size.height / scanlineWidth).toInt() step 3) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.2f),
                        topLeft = androidx.compose.ui.geometry.Offset(0f, i * scanlineWidth),
                        size = androidx.compose.ui.geometry.Size(size.width, scanlineWidth)
                    )
                }
            }
        }
    }
}