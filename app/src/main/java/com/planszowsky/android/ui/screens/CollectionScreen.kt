package com.planszowsky.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.planszowsky.android.R
import com.planszowsky.android.domain.model.AppTheme
import com.planszowsky.android.domain.model.Game
import com.planszowsky.android.ui.theme.*
import com.planszowsky.android.ui.viewmodel.CollectionViewModel
import com.planszowsky.android.util.PixelationTransformation
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel = hiltViewModel(),
    onAddGameClick: () -> Unit,
    onGameClick: (String) -> Unit,
    onRandomizerClick: () -> Unit,
    onScanClick: () -> Unit
) {
    val games by viewModel.games.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    
    val isRetro = appTheme == AppTheme.PIXEL_ART

    // Konfiguracja chowanego paska wyszukiwania
    val searchBarHeight = 70.dp
    val searchBarHeightPx = with(LocalDensity.current) { searchBarHeight.toPx() }
    val searchBarOffsetHeightPx = remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = searchBarOffsetHeightPx.value + delta
                searchBarOffsetHeightPx.value = newOffset.coerceIn(-searchBarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
            .then(if (isRetro) Modifier.retroBackground() else Modifier),
        containerColor = if (isRetro) Color.Transparent else MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (isRetro) {
                    RetroFloatingButton(
                        onClick = onScanClick,
                        color = RetroRed,
                        icon = { PixelCameraIcon(color = Color.White) },
                        buttonSize = 56.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    RetroFloatingButton(
                        onClick = onAddGameClick,
                        color = RetroGreen,
                        icon = { PixelPlusIcon(color = Color.White) },
                        buttonSize = 72.dp
                    )
                } else {
                    SmallFloatingActionButton(
                        onClick = onScanClick,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text("📷", fontSize = 20.sp)
                    }

                    LargeFloatingActionButton(
                        onClick = onAddGameClick,
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_button), modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Główna zawartość
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = searchBarHeight + 16.dp, 
                    start = 12.dp, 
                    end = 12.dp, 
                    bottom = 100.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.collection_title).let { if(isRetro) it.uppercase() else it },
                                style = if (isRetro) MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, color = RetroText) 
                                        else MaterialTheme.typography.headlineLarge,
                            )
                            
                            if (isRetro) {
                                RetroDiceButton(onClick = onRandomizerClick)
                            } else {
                                IconButton(
                                    onClick = onRandomizerClick,
                                    modifier = Modifier.background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                                ) {
                                    Text("🎲", fontSize = 20.sp)
                                }
                            }
                        }

                        if (categories.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(if (isRetro) 4.dp else 8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                item {
                                    if (isRetro) {
                                        RetroFilterChip(
                                            text = stringResource(R.string.all_categories).uppercase(),
                                            isSelected = selectedCategory == null,
                                            onClick = { viewModel.selectCategory(null) }
                                        )
                                    } else {
                                        FilterChip(
                                            selected = selectedCategory == null,
                                            onClick = { viewModel.selectCategory(null) },
                                            label = { Text(stringResource(R.string.all_categories)) }
                                        )
                                    }
                                }
                                items(categories) { category ->
                                    if (isRetro) {
                                        RetroFilterChip(
                                            text = category.uppercase(),
                                            isSelected = selectedCategory == category,
                                            onClick = { viewModel.selectCategory(category) }
                                        )
                                    } else {
                                        FilterChip(
                                            selected = selectedCategory == category,
                                            onClick = { viewModel.selectCategory(category) },
                                            label = { Text(category) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                items(games, key = { it.id }) { game ->
                    GameCard(game, isRetro = isRetro, onClick = { onGameClick(game.id) })
                }
                
                item(span = StaggeredGridItemSpan.FullLine) {
                    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
                    Spacer(modifier = Modifier.height(screenHeight - searchBarHeight))
                }
            }

            // Pływający pasek wyszukiwania (Floating Search Bar)
            if (isRetro) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(searchBarHeight - 16.dp)
                        .offset { IntOffset(x = 0, y = searchBarOffsetHeightPx.value.roundToInt()) }
                        .drawBehind { drawDitheredShadow(size) }
                        .background(RetroElementBackground)
                        .drawBehind {
                            val stroke = 3.dp.toPx()
                            drawRect(RetroBlack, style = Stroke(stroke * 2f))
                            drawRect(RetroGold, style = Stroke(stroke), topLeft = Offset(stroke/2, stroke/2), size = Size(size.width - stroke, size.height - stroke))
                        }
                        .clickable { onAddGameClick() },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixelSearchIcon(
                            color = RetroText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.search_hint).uppercase(),
                            color = RetroText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(searchBarHeight - 16.dp)
                        .offset { IntOffset(x = 0, y = searchBarOffsetHeightPx.value.roundToInt()) }
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .clickable { onAddGameClick() },
                    tonalElevation = 8.dp,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.search_hint), color = Color.Gray)
                    }
                }
            }
            
            // Top Dithering (Status Bar area)
            if (isRetro) {
                Canvas(modifier = Modifier.fillMaxWidth().height(8.dp).align(Alignment.TopCenter)) {
                    drawDitheringPattern(RetroBlack)
                }
            }
        }
    }
}

@Composable
fun RetroDiceButton(onClick: () -> Unit) {
    RetroFloatingButton(
        onClick = onClick,
        color = RetroGold,
        icon = {
            Canvas(modifier = Modifier.size(32.dp)) {
                 drawRect(RetroBlack, Offset(4.dp.toPx(), 4.dp.toPx()), Size(24.dp.toPx(), 24.dp.toPx()))
                 drawRect(Color.White, Offset(6.dp.toPx(), 6.dp.toPx()), Size(20.dp.toPx(), 20.dp.toPx()))
                 drawRect(RetroBlack, Offset(10.dp.toPx(), 10.dp.toPx()), Size(4.dp.toPx(), 4.dp.toPx()))
                 drawRect(RetroBlack, Offset(18.dp.toPx(), 18.dp.toPx()), Size(4.dp.toPx(), 4.dp.toPx()))
            }
        },
        buttonSize = 48.dp
    )
}

@Composable
fun RetroFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .then(if (isSelected) Modifier.drawBehind { drawDitheredShadow(size) } else Modifier)
            .background(if (isSelected) RetroGold else RetroElementBackground)
            .clickable(onClick = onClick)
            .drawBehind {
                val stroke = 2.dp.toPx()
                drawRect(RetroBlack, style = Stroke(stroke * 2f))
                if (isSelected) {
                    drawRect(Color.White.copy(alpha = 0.3f), Offset(stroke, stroke), Size(size.width - stroke*2, stroke))
                }
            }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) RetroBlack else RetroText
            )
        )
    }
}

@Composable
fun GameCard(game: Game, isRetro: Boolean = false, onClick: () -> Unit) {
    if (isRetro) {
        RetroChunkyBox(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            borderColor = if(game.isWishlisted) RetroGold else RetroGrey,
            backgroundColor = RetroBlack
        ) {
            Column {
                Box(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(game.imageUrl ?: game.thumbnailUrl)
                            .transformations(PixelationTransformation(pixelSize = 12))
                            .crossfade(true)
                            .build(),
                        contentDescription = game.title,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth,
                        filterQuality = FilterQuality.None
                    )
                }

                Column(modifier = Modifier.fillMaxWidth().background(RetroBrown).padding(8.dp)) {
                    if (game.isBorrowed) {
                        Surface(
                            color = RetroGold,
                            shape = RectangleShape,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.borrowed_badge).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = RetroBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = game.title.uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                        color = RetroText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).background(Color.DarkGray)
            ) {
                AsyncImage(
                    model = game.imageUrl ?: game.thumbnailUrl,
                    contentDescription = game.title,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, GradientOverlay),
                                startY = 100f
                            )
                        )
                )

                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                ) {
                    if (game.isBorrowed) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.borrowed_badge),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}