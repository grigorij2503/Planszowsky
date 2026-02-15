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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import pl.pointblank.planszowsky.R
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.CollectionViewMode
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.ui.theme.*
import pl.pointblank.planszowsky.ui.viewmodel.CollectionViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel = hiltViewModel(),
    onAddGameClick: () -> Unit,
    onGameClick: (String, String) -> Unit,
    onRandomizerClick: () -> Unit,
    onScanClick: () -> Unit
) {
    val games by viewModel.games.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val viewMode by viewModel.collectionViewMode.collectAsState()
    val activeCollection by viewModel.activeCollection.collectAsState()
    
    val isRetro = appTheme == AppTheme.PIXEL_ART
    var isCategoriesExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val minSearchBarHeight = 72.dp
    val searchBarHeightPx = with(LocalDensity.current) { minSearchBarHeight.toPx() }
    val searchBarOffsetHeightPx = remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -2f || available.y > 2f) {
                    focusManager.clearFocus()
                }
                val delta = available.y
                val newOffset = searchBarOffsetHeightPx.floatValue + delta
                searchBarOffsetHeightPx.floatValue = newOffset.coerceIn(-searchBarHeightPx, 0f)
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
            if (activeCollection?.isReadOnly != true) {
                Column(horizontalAlignment = Alignment.End) {
                    if (isRetro) {
                        val scanColor = Color(0xFF009688)
                        RetroFloatingButton(
                            onClick = onScanClick,
                            color = scanColor,
                            icon = { 
                                Canvas(modifier = Modifier.size(28.dp)) {
                                    val p = size.width / 14
                                    val c = Color.White
                                    drawRect(c, Offset(2*p, 4*p), Size(10*p, 7*p))
                                    drawRect(c, Offset(5*p, 2*p), Size(4*p, 2*p))
                                    drawRect(scanColor, Offset(5*p, 6*p), Size(4*p, 3*p))
                                    drawRect(c, Offset(6*p, 7*p), Size(2*p, 1*p))
                                }
                            },
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
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            val columns = when(viewMode) {
                CollectionViewMode.GRID -> 2
                CollectionViewMode.LIST -> 1
                CollectionViewMode.COMPACT -> 3
            }

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, start = 12.dp, end = 12.dp, bottom = 100.dp),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = (activeCollection?.name ?: stringResource(R.string.collection_title)).let { if(isRetro) it.uppercase() else it },
                                    style = if (isRetro) MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, color = RetroText) 
                                            else MaterialTheme.typography.headlineLarge,
                                )
                                if (activeCollection?.isReadOnly == true) {
                                    Text(
                                        text = stringResource(R.string.readonly_notice).let { if(isRetro) it.uppercase() else it },
                                        style = if (isRetro) MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = RetroRed)
                                                else MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isRetro) {
                                    RetroDiceButton(onClick = onRandomizerClick)
                                } else {
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

                                AnimatedVisibility(
                                    visible = isCategoriesExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    val remainingCategories = categories.drop(2)
                                    Box(modifier = Modifier.padding(top = 8.dp)) {
                                        if (isRetro) {
                                            RetroChunkyBox(backgroundColor = RetroElementBackground, borderColor = RetroBlack, accentColor = RetroGrey) {
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

            // Floating Search Bar
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .heightIn(min = minSearchBarHeight - 16.dp)
                    .offset { IntOffset(x = 0, y = searchBarOffsetHeightPx.floatValue.roundToInt()) }
                    .then(if (isRetro) Modifier.drawBehind { drawDitheredShadow(size) }.background(RetroElementBackground).drawBehind {
                        val stroke = 3.dp.toPx()
                        drawRect(RetroBlack, style = Stroke(stroke * 2f))
                        drawRect(RetroGold, style = Stroke(stroke), topLeft = Offset(stroke/2, stroke/2), size = Size(size.width - stroke, size.height - stroke))
                    } else Modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isRetro) {
                        PixelSearchIcon(color = RetroText, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = RetroText),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(text = stringResource(R.string.search_hint).uppercase(), color = RetroText.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace))
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = RetroText)
                            }
                        }
                        RetroViewModeButton(viewMode = viewMode, onClick = viewModel::toggleViewMode)
                    } else {
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
}

@Composable
fun RetroDiceButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    RetroFloatingButton(
        modifier = modifier,
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
fun CategoryChip(text: String, isSelected: Boolean, isRetro: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (isRetro) {
        Box(
            modifier = modifier.height(38.dp).clickable(onClick = onClick).pixelButtonFrame(isSelected = isSelected, thickness = 2.dp).padding(2.dp).background(if (isSelected) RetroGold else RetroElementBackground).padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text.uppercase(), style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, color = if (isSelected) RetroBlack else RetroText, fontSize = 11.sp))
        }
    } else {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.height(36.dp)
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
            CategoryChip(text = category, isSelected = selectedCategory == category, isRetro = isRetro, onClick = { onCategoryClick(category) })
        }
    }
}

@Composable
fun GameCard(game: Game, isRetro: Boolean = false, onClick: () -> Unit) {
    if (isRetro) {
        Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(4.dp).rpgGameFrame(frameColor = if (game.isWishlisted) RetroGold else RetroElementBackground, thickness = 4.dp).background(RetroBlack)) {
            Box {
                AsyncImage(model = game.imageUrl ?: game.thumbnailUrl, contentDescription = game.title, modifier = Modifier.fillMaxWidth().aspectRatio(0.85f), contentScale = ContentScale.Crop, filterQuality = FilterQuality.None)
                if (game.isBorrowed) {
                    Surface(color = RetroOrange, shape = RectangleShape, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).drawBehind { drawRect(RetroBlack, style = Stroke(2.dp.toPx())) }) {
                        Text(text = "LENT", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, fontSize = 8.sp), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = Color.White)
                    }
                }
            }
            Text(text = game.title.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, color = RetroText, fontSize = 10.sp, lineHeight = 12.sp), maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(8.dp))
        }
    } else {
        Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).background(Color.DarkGray)) {
                AsyncImage(model = game.imageUrl ?: game.thumbnailUrl, contentDescription = game.title, modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
                Box(modifier = Modifier.matchParentSize().background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(0.7f)), startY = 100f)))
                Text(text = game.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomStart).padding(12.dp))
            }
        }
    }
}

@Composable
fun GameListRow(game: Game, isRetro: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = if(isRetro) RectangleShape else RoundedCornerShape(16.dp), color = if(isRetro) RetroElementBackground else MaterialTheme.colorScheme.surface) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = game.thumbnailUrl, contentDescription = null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop, filterQuality = if(isRetro) FilterQuality.None else FilterQuality.Low)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = if(isRetro) game.title.uppercase() else game.title, style = if(isRetro) MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace, color = RetroText) else MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = game.categories.take(2).joinToString(", "), style = MaterialTheme.typography.bodySmall, color = if(isRetro) RetroGold else MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun GameCompactCard(game: Game, isRetro: Boolean, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().aspectRatio(0.8f).clickable(onClick = onClick), shape = if(isRetro) RectangleShape else RoundedCornerShape(12.dp)) {
        AsyncImage(model = game.thumbnailUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, filterQuality = if(isRetro) FilterQuality.None else FilterQuality.Low)
    }
}

@Composable
fun RetroViewModeButton(viewMode: CollectionViewMode, onClick: () -> Unit) {
    RetroFloatingButton(onClick = onClick, color = RetroGold, buttonSize = 40.dp, icon = {
        Text(when(viewMode) {
            CollectionViewMode.GRID -> "田"
            CollectionViewMode.LIST -> "≡"
            CollectionViewMode.COMPACT -> "⠿"
        }, fontWeight = FontWeight.Bold, color = RetroBlack)
    })
}
