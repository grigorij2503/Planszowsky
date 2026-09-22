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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val games by viewModel.wishlistedGames.collectAsStateWithLifecycle()
    val isRetro = appTheme == AppTheme.PIXEL_ART

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .then(if (isRetro) Modifier.retroBackground() else Modifier.background(MaterialTheme.colorScheme.background))
            .padding(if (isRetro) 10.dp else 16.dp)
    ) {
        if (isRetro) {
            RetroScreenTitle(title = stringResource(R.string.wishlist_title))
            Spacer(modifier = Modifier.height(14.dp))
        } else {
            Text(
                text = stringResource(R.string.wishlist_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
            )
        }

        if (games.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (isRetro) {
                    RetroPanel(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(modifier = Modifier.size(42.dp)) {
                                PixelShinyHeartIcon(isSelected = false)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.wishlist_empty).uppercase(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    color = RetroText,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.wishlist_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(if (isRetro) 3 else 2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(if (isRetro) 6.dp else 12.dp),
                verticalItemSpacing = if (isRetro) 8.dp else 12.dp
            ) {
                items(games) { game ->
                    GameCard(game, isRetro = isRetro, onClick = { onGameClick(game.id, game.collectionId) })
                }
            }
        }
    }
}
