package com.planszowsky.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.planszowsky.android.ui.viewmodel.WishlistViewModel

@Composable
fun WishlistScreen(
    viewModel: WishlistViewModel = hiltViewModel(),
    onGameClick: (String) -> Unit
) {
    val games by viewModel.wishlistedGames.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Text(
                    text = "Wishlist",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            if (games.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Twoja lista życzeń jest pusta.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }

            items(games) { game ->
                GameCard(game, onClick = { onGameClick(game.id) })
            }
        }
    }
}
