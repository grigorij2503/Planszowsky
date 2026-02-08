package pl.pointblank.planszowsky.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pl.pointblank.planszowsky.R
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.ui.theme.*
import pl.pointblank.planszowsky.ui.viewmodel.CollectionViewModel
import pl.pointblank.planszowsky.util.PixelationTransformation
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
    val searchQuery by viewModel.searchQuery.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    
    val isRetro = appTheme == AppTheme.PIXEL_ART
    var isCategoriesExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Konfiguracja chowanego paska wyszukiwania
    val minSearchBarHeight = 72.dp
    val searchBarHeightPx = with(LocalDensity.current) { minSearchBarHeight.toPx() }
    val searchBarOffsetHeightPx = remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Hide keyboard on scroll
                if (available.y < -2f || available.y > 2f) {
                    focusManager.clearFocus()
                }
                
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
            .then(if (isRetro) Modifier.retroBackground().retroCrtEffect() else Modifier),
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
                    top = 16.dp, 
                    start = 12.dp, 
                    end = 12.dp, 
                    bottom = 100.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Spacer(modifier = Modifier.height(minSearchBarHeight))
                }

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
                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                // Top row: All + Top 2 + Expand Button - Scrollable for small screens
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(if (isRetro) 4.dp else 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    item {
                                        // All Categories Chip
                                        CategoryChip(
                                            text = stringResource(R.string.all_categories),
                                            isSelected = selectedCategory == null,
                                            isRetro = isRetro,
                                            onClick = { viewModel.selectCategory(null) }
                                        )
                                    }

                                    items(categories.take(2)) { category ->
                                        CategoryChip(
                                            text = category,
                                            isSelected = selectedCategory == category,
                                            isRetro = isRetro,
                                            onClick = { viewModel.selectCategory(category) }
                                        )
                                    }

                                    if (categories.size > 2) {
                                        item {
                                            CategoryChip(
                                                text = if (isCategoriesExpanded) "▲" else "...",
                                                isSelected = isCategoriesExpanded,
                                                isRetro = isRetro,
                                                onClick = { isCategoriesExpanded = !isCategoriesExpanded }
                                            )
                                        }
                                    }
                                }

                                // Expanded Panel
                                AnimatedVisibility(
                                    visible = isCategoriesExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    val remainingCategories = categories.drop(2)
                                    
                                    Box(modifier = Modifier.padding(top = 8.dp)) {
                                        if (isRetro) {
                                            RetroChunkyBox(
                                                backgroundColor = RetroElementBackground,
                                                borderColor = RetroGrey,
                                                showShadow = false
                                            ) {
                                                ExpandedCategoriesFlow(
                                                    categories = remainingCategories,
                                                    selectedCategory = selectedCategory,
                                                    isRetro = true,
                                                    onCategoryClick = viewModel::selectCategory
                                                )
                                            }
                                        } else {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                ExpandedCategoriesFlow(
                                                    categories = remainingCategories,
                                                    selectedCategory = selectedCategory,
                                                    isRetro = false,
                                                    onCategoryClick = viewModel::selectCategory
                                                )
                                            }
                                        }
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = R.drawable.bgg,
                            contentDescription = "Powered by BGG",
                            modifier = Modifier.height(32.dp),
                            contentScale = ContentScale.Fit,
                            alpha = if (isRetro) 0.7f else 0.4f,
                            filterQuality = if (isRetro) FilterQuality.None else FilterQuality.Low
                        )
                    }
                }
            }

            // Pływający pasek wyszukiwania (Floating Search Bar)
            if (isRetro) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .heightIn(min = minSearchBarHeight - 16.dp)
                        .offset { IntOffset(x = 0, y = searchBarOffsetHeightPx.value.roundToInt()) }
                        .drawBehind { drawDitheredShadow(size) }
                        .background(RetroElementBackground)
                        .drawBehind {
                            val stroke = 3.dp.toPx()
                            drawRect(RetroBlack, style = Stroke(stroke * 2f))
                            drawRect(RetroGold, style = Stroke(stroke), topLeft = Offset(stroke/2, stroke/2), size = Size(size.width - stroke, size.height - stroke))
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixelSearchIcon(
                            color = RetroText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = RetroText),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.search_hint).uppercase(),
                                        color = RetroText.copy(alpha = 0.5f),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = RetroText)
                            }
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .heightIn(min = minSearchBarHeight - 16.dp)
                        .offset { IntOffset(x = 0, y = searchBarOffsetHeightPx.value.roundToInt()) }
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    tonalElevation = 8.dp,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = { Text(stringResource(R.string.search_hint)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                            }
                        }
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
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    isRetro: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isRetro) {
        RetroFilterChip(
            text = text.uppercase(),
            isSelected = isSelected,
            onClick = onClick
        )
    } else {
        FilterChip(
            selected = isSelected,
            onClick = onClick,
            label = { Text(text, maxLines = 1) },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandedCategoriesFlow(
    categories: List<String>,
    selectedCategory: String?,
    isRetro: Boolean,
    onCategoryClick: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(if (isRetro) 4.dp else 8.dp),
        verticalArrangement = Arrangement.spacedBy(if (isRetro) 4.dp else 8.dp)
    ) {
        categories.forEach { category ->
            CategoryChip(
                text = category,
                isSelected = selectedCategory == category,
                isRetro = isRetro,
                onClick = { onCategoryClick(category) }
            )
        }
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
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).drawBehind {
                        drawDitheredOverlay(alpha = 0.2f)
                    }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(game.imageUrl ?: game.thumbnailUrl)
                            .transformations(PixelationTransformation(pixelSize = 4))
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
