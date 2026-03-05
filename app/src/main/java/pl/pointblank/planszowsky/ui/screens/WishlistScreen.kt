package pl.pointblank.planszowsky.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import pl.pointblank.planszowsky.R
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.ui.theme.*
import pl.pointblank.planszowsky.ui.viewmodel.WishlistViewModel

@Composable
fun WishlistScreen(
    appTheme: AppTheme = AppTheme.MODERN,
    viewModel: WishlistViewModel = hiltViewModel(),
    onGameClick: (String, String) -> Unit
) {
    val games by viewModel.wishlistedGames.collectAsState()
    val isRetro = appTheme == AppTheme.PIXEL_ART

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .then(if (isRetro) Modifier.retroBackground() else Modifier.background(MaterialTheme.colorScheme.background))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.wishlist_title).let { if (isRetro) it.uppercase() else it },
            style = if (isRetro) 
                MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, color = RetroGold)
                else MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
        )

        if (games.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.wishlist_empty).let { if(isRetro) it.uppercase() else it },
                    style = if (isRetro) MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else MaterialTheme.typography.bodyLarge,
                    color = if (isRetro) RetroText else Color.Gray
                )
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                items(games) { game ->
                    GameCard(game, isRetro = isRetro, onClick = { onGameClick(game.id, game.collectionId) })
                }
            }
        }
    }
}
