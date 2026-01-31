package com.planszowsky.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.planszowsky.android.domain.model.Game
import com.planszowsky.android.ui.theme.GradientOverlay
import com.planszowsky.android.ui.viewmodel.CollectionViewModel
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
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // Mały przycisk skanowania nad głównym FABem dla prostoty (zamiast pełnego expandera na razie)
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
                    Icon(Icons.Default.Add, contentDescription = "Dodaj", modifier = Modifier.size(36.dp))
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Twoja Kolekcja",
                            style = MaterialTheme.typography.headlineLarge,
                        )
                        
                        IconButton(
                            onClick = onRandomizerClick,
                            modifier = Modifier.background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                        ) {
                            Text("🎰", fontSize = 20.sp)
                        }
                    }
                }
                items(games) { game ->
                    GameCard(game, onClick = { onGameClick(game.id) })
                }
            }

            // Pływający pasek wyszukiwania (Floating Search Bar)
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(searchBarHeight - 16.dp)
                    .offset { IntOffset(x = 0, y = searchBarOffsetHeightPx.value.roundToInt()) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                tonalElevation = 8.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onAddGameClick() }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Szukaj w swojej kolekcji...", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun GameCard(game: Game, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .background(Color.DarkGray)
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
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                if (game.isBorrowed) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "POŻYCZONA",
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