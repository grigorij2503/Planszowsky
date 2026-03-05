package pl.pointblank.planszowsky.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import pl.pointblank.planszowsky.R
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.ui.theme.*
import pl.pointblank.planszowsky.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    appTheme: AppTheme = AppTheme.MODERN,
    initialQuery: String? = null,
    viewModel: SearchViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val additionSuccess by viewModel.additionSuccess.collectAsState()
    val isRetro = appTheme == AppTheme.PIXEL_ART
    
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(initialQuery) {
        if (initialQuery != null) {
            viewModel.onQueryChange(initialQuery)
        }
        // Small delay ensures the view is ready to receive focus
        kotlinx.coroutines.delay(100)
        focusRequester.requestFocus()
    }

    LaunchedEffect(additionSuccess) {
        if (additionSuccess) {
            onBackClick()
        }
    }

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .then(if (isRetro) Modifier.retroBackground() else Modifier),
        containerColor = if (isRetro) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            if (isRetro) {
                RetroSearchTopBar(
                    query = query,
                    onQueryChange = viewModel::onQueryChange,
                    onBackClick = onBackClick,
                    focusRequester = focusRequester
                )
            } else {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = query,
                            onValueChange = viewModel::onQueryChange,
                            placeholder = { Text(stringResource(R.string.search_bgg_hint)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp)
                                .focusRequester(focusRequester),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button))
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = if (isRetro) RetroGold else MaterialTheme.colorScheme.primary
                )
            } else if (error != null) {
                Text(
                    text = stringResource(R.string.bgg_api_error),
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    style = if (isRetro) MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroRed)
                            else MaterialTheme.typography.bodyLarge,
                    color = if (isRetro) RetroRed else MaterialTheme.colorScheme.error,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else if (results.isEmpty() && query.isNotBlank()) {
                Text(
                    text = stringResource(R.string.no_results),
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    style = if (isRetro) MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText)
                            else MaterialTheme.typography.bodyLarge,
                    color = if (isRetro) RetroText else Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(results) { game ->
                        SearchResultCard(
                            game = game,
                            isRetro = isRetro,
                            onAddToCollection = { viewModel.addToCollection(game) },
                            onAddToWishlist = { viewModel.addToWishlist(game) }
                        )
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = R.drawable.bgg,
                                contentDescription = "Powered by BGG",
                                modifier = Modifier.height(40.dp),
                                contentScale = ContentScale.Fit,
                                filterQuality = if (isRetro) FilterQuality.None else FilterQuality.Low
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RetroSearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RetroSquareIconButton(onClick = onBackClick, color = RetroElementBackground) {
            PixelBackIcon(color = RetroText)
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(RetroElementBackground)
                .drawBehind {
                    val stroke = 3.dp.toPx()
                    drawRect(RetroBlack, style = Stroke(stroke * 2f))
                    drawRect(RetroGold, style = Stroke(stroke), topLeft = Offset(stroke/2, stroke/2), size = Size(size.width - stroke, size.height - stroke))
                }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_bgg_hint).uppercase(),
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText.copy(alpha = 0.5f))
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
fun SearchResultCard(
    game: Game, 
    isRetro: Boolean, 
    onAddToCollection: () -> Unit,
    onAddToWishlist: () -> Unit
) {
    if (isRetro) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rpgGameFrame(
                    frameColor = if (game.isWishlisted) RetroGold else RetroElementBackground,
                    thickness = 4.dp
                )
                .background(RetroBlack)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = game.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.None
                )

                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.title.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace, 
                            color = RetroText,
                            fontSize = 14.sp
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = game.yearPublished ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace, 
                            color = RetroGold
                        )
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RetroSquareIconButton(onClick = onAddToWishlist, color = RetroRed) {
                        Box(modifier = Modifier.size(24.dp)) {
                            PixelHeart24(color = Color.White)
                        }
                    }
                    RetroSquareIconButton(onClick = onAddToCollection, color = RetroGreen) {
                        Box(modifier = Modifier.size(24.dp)) {
                            PixelPlus24(color = Color.White)
                        }
                    }
                }
            }
        }
    } else {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = game.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = game.yearPublished ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onAddToWishlist) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = stringResource(R.string.add_to_wishlist))
                    }
                    IconButton(onClick = onAddToCollection) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_button))
                    }
                }
            }
        }
    }
}
