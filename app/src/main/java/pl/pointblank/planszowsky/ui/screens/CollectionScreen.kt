package pl.pointblank.planszowsky.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import pl.pointblank.planszowsky.R
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.CollectionViewMode
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.ui.theme.*
import pl.pointblank.planszowsky.ui.viewmodel.CollectionViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel = hiltViewModel(),
    onAddGameClick: () -> Unit,
    onGameClick: (String, String) -> Unit,
    onRandomizerClick: () -> Unit,
    onScanClick: () -> Unit
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val viewMode by viewModel.collectionViewMode.collectAsStateWithLifecycle()
    val activeCollection by viewModel.activeCollection.collectAsStateWithLifecycle()
    
    val isRetro = appTheme == AppTheme.PIXEL_ART
    var isCategoriesExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val minSearchBarHeight = 72.dp
    val searchBarHeightPx = with(LocalDensity.current) { minSearchBarHeight.toPx() }
    val searchBarOffsetHeightPx = remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Only clear focus if scrolling significantly
                if (available.y < -10f || available.y > 10f) {
                    focusManager.clearFocus()
                }
                
                // Only hide search bar if we are NOT at the top of the list
                // For simplicity, we just keep the existing logic but make it less sensitive
                val delta = available.y
                val newOffset = searchBarOffsetHeightPx.floatValue + delta
                
                // Allow search bar to be visible (0) or hidden (-searchBarHeightPx)
                // We add a small buffer to prevent jitter
                searchBarOffsetHeightPx.floatValue = newOffset.coerceIn(-searchBarHeightPx, 0f)
                
                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .then(if (isRetro) Modifier else Modifier.nestedScroll(nestedScrollConnection))
            .then(if (isRetro) Modifier.retroBackground() else Modifier),
        containerColor = if (isRetro) Color.Transparent else MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (activeCollection?.isReadOnly != true) {
                Column(horizontalAlignment = Alignment.End) {
                    if (isRetro) {
                        RetroOrbButton(
                            onClick = onScanClick,
                            color = RetroStone,
                            icon = { 
                                Canvas(modifier = Modifier.size(28.dp)) {
                                    val p = size.width / 14
                                    val c = RetroText
                                    drawRect(c, Offset(2*p, 4*p), Size(10*p, 7*p))
                                    drawRect(c, Offset(5*p, 2*p), Size(4*p, 2*p))
                                    drawRect(RetroStone, Offset(5*p, 6*p), Size(4*p, 3*p))
                                    drawRect(c, Offset(6*p, 7*p), Size(2*p, 1*p))
                                }
                            },
                            buttonSize = 54.dp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        RetroOrbButton(
                            onClick = onAddGameClick,
                            color = RetroCrimson,
                            icon = { PixelPlusIcon(color = RetroText) },
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
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            val columns = when(viewMode) {
                CollectionViewMode.GRID -> if (isRetro) 3 else 2
                CollectionViewMode.LIST -> 1
                CollectionViewMode.COMPACT -> 3
            }

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = if (isRetro) 6.dp else 16.dp,
                    start = if (isRetro) 8.dp else 12.dp,
                    end = if (isRetro) 8.dp else 12.dp,
                    bottom = 108.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(if (isRetro) 6.dp else 12.dp),
                verticalItemSpacing = if (isRetro) 8.dp else 12.dp
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    if (isRetro) {
                        Spacer(modifier = Modifier.height(4.dp))
                    } else {
                        Spacer(
                            modifier = Modifier
                                .statusBarsPadding()
                                .height(minSearchBarHeight)
                        )
                    }
                }

                item(span = StaggeredGridItemSpan.FullLine) {
                    Column {
                        if (isRetro) {
                            RetroCollectionHeader(
                                title = activeCollection?.name ?: stringResource(R.string.collection_title),
                                gameCount = games.size,
                                isReadOnly = activeCollection?.isReadOnly == true,
                                onRandomizerClick = onRandomizerClick
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            RetroCollectionSearchBar(
                                value = searchQuery,
                                onValueChange = viewModel::onSearchQueryChange,
                                onClear = { viewModel.onSearchQueryChange("") },
                                viewMode = viewMode,
                                onToggleViewMode = viewModel::toggleViewMode
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeCollection?.name ?: stringResource(R.string.collection_title),
                                        style = MaterialTheme.typography.headlineLarge,
                                    )
                                    if (activeCollection?.isReadOnly == true) {
                                        Text(
                                            text = stringResource(R.string.readonly_notice),
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = viewModel::toggleViewMode,
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    ) {
                                        Text(when(viewMode) {
                                            CollectionViewMode.GRID -> "📱"
                                            CollectionViewMode.LIST -> "🗒️"
                                            CollectionViewMode.COMPACT -> "🔡"
                                        }, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = onRandomizerClick,
                                        modifier = Modifier.background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                                    ) {
                                        Text("🎲", fontSize = 20.sp)
                                    }
                                }
                            }
                        }

                        if (categories.isNotEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(if (isRetro) 4.dp else 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    item {
                                        CategoryChip(
                                            text = stringResource(R.string.all_categories),
                                            isSelected = selectedCategory == null,
                                            isRetro = isRetro,
                                            onClick = { viewModel.selectCategory(null) }
                                        )
                                    }
                                    items(categories.take(2)) { category ->
                                        CategoryChip(
                                            text = categoryDisplayName(category),
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

                                AnimatedVisibility(
                                    visible = isCategoriesExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    val remainingCategories = categories.drop(2)
                                    Box(modifier = Modifier.padding(top = 8.dp)) {
                                        if (isRetro) {
                                            RetroChunkyBox(backgroundColor = RetroStone, borderColor = RetroIron, accentColor = RetroBronze) {
                                                ExpandedCategoriesFlow(categories = remainingCategories, selectedCategory = selectedCategory, isRetro = true, onCategoryClick = viewModel::selectCategory)
                                            }
                                        } else {
                                            Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                                ExpandedCategoriesFlow(categories = remainingCategories, selectedCategory = selectedCategory, isRetro = false, onCategoryClick = viewModel::selectCategory)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                items(games, key = { it.id }) { game ->
                    when(viewMode) {
                        CollectionViewMode.GRID -> GameCard(game, isRetro = isRetro, onClick = { onGameClick(game.id, game.collectionId) })
                        CollectionViewMode.LIST -> GameListRow(game, isRetro = isRetro, onClick = { onGameClick(game.id, game.collectionId) })
                        CollectionViewMode.COMPACT -> GameCompactCard(game, isRetro = isRetro, onClick = { onGameClick(game.id, game.collectionId) })
                    }
                }
                
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        AsyncImage(model = R.drawable.bgg, contentDescription = "Powered by BGG", modifier = Modifier.height(32.dp), contentScale = ContentScale.Fit, alpha = if (isRetro) 0.7f else 0.4f, filterQuality = if (isRetro) FilterQuality.None else FilterQuality.Low)
                    }
                }
            }

            // The modern theme keeps its collapsible search bar. In Retro it is
            // part of the inventory header so the hierarchy matches the mockup.
            if (!isRetro) Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .heightIn(min = minSearchBarHeight - 16.dp)
                    .offset { IntOffset(x = 0, y = searchBarOffsetHeightPx.floatValue.roundToInt()) }
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(text = stringResource(R.string.search_hint), color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RetroCollectionHeader(
    title: String,
    gameCount: Int,
    isReadOnly: Boolean,
    onRandomizerClick: () -> Unit
) {
    RetroPanel(
        modifier = Modifier.fillMaxWidth(),
        accentColor = RetroBronzeLight,
        backgroundColors = listOf(RetroOxblood, RetroRed.copy(alpha = 0.72f), RetroOxblood),
        cornerCut = 14.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 13.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "◆",
                color = RetroBronzeLight,
                fontSize = 12.sp,
                lineHeight = 12.sp
            )
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.ExtraBold,
                    color = RetroText,
                    fontSize = 25.sp,
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .pixelFrame(
                        borderColor = RetroBlack,
                        innerColor = RetroBronze.copy(alpha = 0.7f),
                        thickness = 1.dp
                    )
                    .background(RetroIron)
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.collection_game_count,
                        gameCount,
                        gameCount
                    ).uppercase(),
                    color = RetroBronzeLight,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.8.sp
                )
            }
            if (isReadOnly) {
                Text(
                    text = stringResource(R.string.readonly_notice).uppercase(),
                    modifier = Modifier.padding(top = 6.dp),
                    color = RetroParchment,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        RetroDiceButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = onRandomizerClick
        )
    }
}

@Composable
private fun RetroCollectionSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    viewMode: CollectionViewMode,
    onToggleViewMode: () -> Unit
) {
    RetroPanel(
        modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
        accentColor = RetroBronze,
        cornerCut = 9.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RetroGeneratedIcon(
                drawableRes = R.drawable.retro_icon_search,
                modifier = Modifier.size(34.dp),
            )
            Spacer(modifier = Modifier.width(7.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = RetroText,
                    fontWeight = FontWeight.Medium
                ),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_hint),
                            color = RetroParchment.copy(alpha = 0.62f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                    innerTextField()
                }
            )
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = RetroText)
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            RetroViewModeButton(viewMode = viewMode, onClick = onToggleViewMode)
        }
    }
}

@Composable
fun RetroDiceButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    RetroOrbButton(
        modifier = modifier,
        onClick = onClick,
        color = RetroEmber,
        icon = {
            RetroGeneratedIcon(
                drawableRes = R.drawable.retro_icon_dice,
                modifier = Modifier.size(42.dp),
            )
        },
        buttonSize = 46.dp
    )
}

@Composable
fun CategoryChip(text: String, isSelected: Boolean, isRetro: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (isRetro) {
        RetroFilterChip(
            text = text,
            isSelected = isSelected,
            onClick = onClick,
            modifier = modifier.heightIn(min = 42.dp)
        )
    } else {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.heightIn(min = 36.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                Text(text = text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandedCategoriesFlow(categories: List<String>, selectedCategory: String?, isRetro: Boolean, onCategoryClick: (String) -> Unit) {
    FlowRow(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(if (isRetro) 4.dp else 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        categories.forEach { category ->
            CategoryChip(text = categoryDisplayName(category), isSelected = selectedCategory == category, isRetro = isRetro, onClick = { onCategoryClick(category) })
        }
    }
}

@Composable
fun BorrowBadge(
    game: Game,
    isRetro: Boolean,
    modifier: Modifier = Modifier
) {
    val isLent = game.isBorrowed
    val isBorrowedFrom = game.isBorrowedFrom
    val color = if (isLent) RetroOrange else RetroBlue
    
    val name = if (isLent) game.borrowedTo else game.borrowedFrom
    val prefix = if (isLent) "U:" else "OD:"
    val fallback = if (isLent) "U KOGOŚ" else "OD KOGOŚ"
    
    val displayText = if (!name.isNullOrBlank()) {
        "$prefix ${name.trim()}"
    } else {
        fallback
    }
    
    if (isRetro) {
        Surface(
            color = color,
            shape = RectangleShape,
            modifier = modifier
                .drawBehind { drawRect(RetroBlack, style = Stroke(2.dp.toPx())) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(10.dp)) {
                    PixelSwap24(color = Color.White)
                }
                Text(
                    text = displayText.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 8.sp
                    ),
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    } else {
        Surface(
            color = color.copy(alpha = 0.9f),
            shape = RoundedCornerShape(8.dp),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 100.dp)
                )
            }
        }
    }
}

@Composable
fun GameCard(game: Game, isRetro: Boolean = false, onClick: () -> Unit) {
    if (isRetro) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(2.dp)
                .rpgGameFrame(
                    frameColor = if (game.isWishlisted) RetroGold else RetroBronze,
                    thickness = 3.dp
                )
                .background(RetroIron)
        ) {
            Box {
                AsyncImage(
                    model = game.localImageUri ?: game.imageUrl ?: game.thumbnailUrl,
                    contentDescription = game.title,
                    modifier = Modifier.fillMaxWidth().aspectRatio(0.78f),
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.Low
                )
                if (game.isBorrowed || game.isBorrowedFrom) {
                    BorrowBadge(
                        game = game,
                        isRetro = true,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    )
                }
            }
            Text(
                text = game.title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = RetroText,
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    letterSpacing = 0.2.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 34.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(RetroStone, RetroElementBackground, RetroBlack)
                        )
                    )
                    .padding(horizontal = 5.dp, vertical = 7.dp)
            )
        }
    } else {
        Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).background(Color.DarkGray)) {
                AsyncImage(model = game.localImageUri ?: game.imageUrl ?: game.thumbnailUrl, contentDescription = game.title, modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
                Box(modifier = Modifier.matchParentSize().background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(0.7f)), startY = 100f)))
                
                if (game.isBorrowed || game.isBorrowedFrom) {
                    BorrowBadge(
                        game = game,
                        isRetro = false,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    )
                }
                
                Text(text = game.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomStart).padding(12.dp))
            }
        }
    }
}

@Composable
fun GameListRow(game: Game, isRetro: Boolean, onClick: () -> Unit) {
    if (isRetro) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .rpgGameFrame(
                    frameColor = if (game.isWishlisted) RetroGold else RetroBronze,
                    thickness = 3.dp
                )
                .background(
                    Brush.horizontalGradient(
                        listOf(RetroStone, RetroElementBackground, RetroIron)
                    )
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AsyncImage(
                    model = game.localImageUri ?: game.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.Low
                )
                if (game.isBorrowed || game.isBorrowedFrom) {
                    BorrowBadge(
                        game = game,
                        isRetro = true,
                        modifier = Modifier.align(Alignment.BottomCenter).offset(y = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.title.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Serif,
                        color = RetroText,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = categoryDisplayNames(game.categories.take(2)),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = RetroBronzeLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        return
    }

    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = if(isRetro) RectangleShape else RoundedCornerShape(16.dp), color = if(isRetro) RetroElementBackground else MaterialTheme.colorScheme.surface) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                AsyncImage(model = game.localImageUri ?: game.thumbnailUrl, contentDescription = null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop, filterQuality = if(isRetro) FilterQuality.None else FilterQuality.Low)
                if (game.isBorrowed || game.isBorrowedFrom) {
                    BorrowBadge(
                        game = game,
                        isRetro = isRetro,
                        modifier = Modifier.align(Alignment.BottomCenter).offset(y = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = if(isRetro) game.title.uppercase() else game.title, style = if(isRetro) MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace, color = RetroText) else MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = categoryDisplayNames(game.categories.take(2)), style = MaterialTheme.typography.bodySmall, color = if(isRetro) RetroGold else MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun GameCompactCard(game: Game, isRetro: Boolean, onClick: () -> Unit) {
    if (isRetro) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .clickable(onClick = onClick)
                .rpgGameFrame(
                    frameColor = if (game.isWishlisted) RetroGold else RetroBronze,
                    thickness = 3.dp
                )
                .background(RetroIron)
        ) {
            AsyncImage(
                model = game.localImageUri ?: game.thumbnailUrl,
                contentDescription = game.title,
                modifier = Modifier.fillMaxSize().padding(4.dp),
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.Low
            )
            if (game.isBorrowed || game.isBorrowedFrom) {
                BorrowBadge(
                    game = game,
                    isRetro = true,
                    modifier = Modifier.align(Alignment.TopEnd).padding(5.dp)
                )
            }
        }
        return
    }

    Card(modifier = Modifier.fillMaxWidth().aspectRatio(0.8f).clickable(onClick = onClick), shape = if(isRetro) RectangleShape else RoundedCornerShape(12.dp)) {
        Box {
            AsyncImage(model = game.localImageUri ?: game.thumbnailUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, filterQuality = if(isRetro) FilterQuality.None else FilterQuality.Low)
            if (game.isBorrowed || game.isBorrowedFrom) {
                BorrowBadge(
                    game = game,
                    isRetro = isRetro,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun RetroViewModeButton(viewMode: CollectionViewMode, onClick: () -> Unit) {
    RetroFloatingButton(onClick = onClick, color = RetroStone, buttonSize = 40.dp, icon = {
        Text(when(viewMode) {
            CollectionViewMode.GRID -> "田"
            CollectionViewMode.LIST -> "≡"
            CollectionViewMode.COMPACT -> "⠿"
        }, fontWeight = FontWeight.Bold, color = RetroBronzeLight)
    })
}
