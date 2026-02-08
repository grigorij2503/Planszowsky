package pl.pointblank.planszowsky.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
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
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.ui.theme.*
import pl.pointblank.planszowsky.ui.viewmodel.DetailsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val game by viewModel.game.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val isExpertChatEnabled by viewModel.isExpertChatEnabled.collectAsState()
    val isRetro = appTheme == AppTheme.PIXEL_ART
    
    var showChat by remember { mutableStateOf(false) }

    if (showChat && game != null) {
        ExpertChatBottomSheet(
            gameTitle = game!!.title,
            onDismiss = { showChat = false }
        )
    }

    Scaffold(
        modifier = Modifier.then(if (isRetro) Modifier.retroBackground() else Modifier),
        containerColor = if (isRetro) Color.Transparent else MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (game != null && isExpertChatEnabled) {
                if (isRetro) {
                    RetroFloatingButton(
                        onClick = { showChat = true },
                        color = RetroBlue,
                        icon = { PixelChatIcon(color = Color.White) },
                        buttonSize = 64.dp
                    )
                } else {
                    FloatingActionButton(
                        onClick = { showChat = true },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = stringResource(R.string.expert_chat_fab)
                        )
                    }
                }
            }
        },
        topBar = {
            if (isRetro) {
                RetroDetailsTopBar(
                    isWishlisted = game?.isWishlisted == true,
                    isFavorite = game?.isFavorite == true,
                    onBackClick = onBackClick,
                    onWishlistClick = { viewModel.toggleWishlist() },
                    onFavoriteClick = { viewModel.toggleFavorite() },
                    onDeleteClick = {
                        viewModel.deleteGame()
                        onBackClick()
                    }
                )
            } else {
                TopAppBar(
                    title = { },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.background(Color.Black.copy(0.3f), RoundedCornerShape(50))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button))
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleFavorite() },
                            modifier = Modifier.background(Color.Black.copy(0.3f), RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = if (game?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (game?.isFavorite == true) Color.Red else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.toggleWishlist() },
                            modifier = Modifier.background(Color.Black.copy(0.3f), RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = if (game?.isWishlisted == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Wishlist",
                                tint = if (game?.isWishlisted == true) RetroGold else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { 
                                viewModel.deleteGame()
                                onBackClick()
                            },
                            modifier = Modifier.background(Color.Black.copy(0.3f), RoundedCornerShape(50))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_button))
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        game?.let { g ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Image
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                        .then(if (isRetro) Modifier.drawBehind { 
                            drawRect(RetroBlack, style = Stroke(4.dp.toPx())) 
                        } else Modifier)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(g.imageUrl)
                            .size(256, 256)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        filterQuality = if (isRetro) FilterQuality.None else FilterQuality.Low
                    )
                }
                
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (!isRetro) Modifier.offset(y = (-32).dp) else Modifier),
                    shape = if (isRetro) RectangleShape else RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = if (isRetro) RetroBackground else MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = if (isRetro) g.title.uppercase() else g.title,
                            style = if (isRetro) MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, color = RetroText)
                                    else MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = g.yearPublished ?: "",
                            style = if (isRetro) MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = RetroGold)
                                    else MaterialTheme.typography.bodyMedium,
                            color = if (isRetro) RetroGold else MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Chips row
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetadataChip(icon = "👥", label = "${g.minPlayers}-${g.maxPlayers}", isRetro = isRetro)
                            MetadataChip(icon = "⏳", label = "${g.playingTime}m", isRetro = isRetro)
                            MetadataChip(icon = "🧠", label = "3.5/5", isRetro = isRetro)
                        }
                        
                        if (g.categories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                g.categories.forEach { category ->
                                    CategoryChip(category, isRetro = isRetro)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        BorrowingSection(
                            isActive = g.isBorrowed,
                            personName = g.borrowedTo ?: "",
                            labelRes = R.string.borrowed_label,
                            prefixRes = R.string.borrowed_to_prefix,
                            noPersonRes = R.string.no_borrower_desc,
                            dialogTitleRes = R.string.borrow_dialog_title,
                            isRetro = isRetro,
                            onStatusChange = { isActive, name ->
                                viewModel.updateBorrowedStatus(isActive, name)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BorrowingSection(
                            isActive = g.isBorrowedFrom,
                            personName = g.borrowedFrom ?: "",
                            labelRes = R.string.borrowed_from_label,
                            prefixRes = R.string.borrowed_from_prefix,
                            noPersonRes = R.string.no_borrowed_from_desc,
                            dialogTitleRes = R.string.borrow_from_dialog_title,
                            isRetro = isRetro,
                            onStatusChange = { isActive, name ->
                                viewModel.updateBorrowedFromStatus(isActive, name)
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        NotesSection(
                            notes = g.notes ?: "",
                            isRetro = isRetro,
                            onNotesChange = { notes ->
                                viewModel.updateNotes(notes)
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = stringResource(R.string.description_label).let { if(isRetro) it.uppercase() else it },
                            style = if (isRetro) MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RetroText)
                                    else MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = g.description ?: stringResource(R.string.no_description),
                            style = if (isRetro) MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText)
                                    else MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            color = if (isRetro) RetroText else Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = R.drawable.bgg,
                                contentDescription = "Powered by BGG",
                                modifier = Modifier.height(24.dp),
                                contentScale = ContentScale.Fit,
                                alpha = if (isRetro) 0.6f else 0.3f,
                                filterQuality = if (isRetro) FilterQuality.None else FilterQuality.Low
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(100.dp)) // Extra space
                    }
                }
            }
        }
    }
}

@Composable
fun RetroDetailsTopBar(
    isWishlisted: Boolean,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RetroSquareIconButton(onClick = onBackClick, color = RetroElementBackground) {
            PixelBackIcon(color = RetroText)
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RetroSquareIconButton(onClick = onFavoriteClick, color = if (isFavorite) RetroRed else RetroElementBackground) {
                PixelHeartIcon(isSelected = isFavorite)
            }
            RetroSquareIconButton(onClick = onWishlistClick, color = if (isWishlisted) RetroGold else RetroElementBackground) {
                PixelBookmarkIcon(isSelected = isWishlisted, color = if(isWishlisted) RetroBlack else RetroText)
            }
            RetroSquareIconButton(onClick = onDeleteClick, color = RetroElementBackground) {
                PixelDeleteIcon(color = RetroText)
            }
        }
    }
}

@Composable
fun BorrowingSection(
    isActive: Boolean,
    personName: String,
    labelRes: Int,
    prefixRes: Int,
    noPersonRes: Int,
    dialogTitleRes: Int,
    isRetro: Boolean,
    onStatusChange: (Boolean, String?) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var tempPersonName by remember(personName) { mutableStateOf(personName) }

    Surface(
        color = if (isRetro) RetroElementBackground else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = if (isRetro) RectangleShape else RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isRetro) Modifier.drawBehind { 
                drawRect(RetroBlack, style = Stroke(3.dp.toPx())) 
            } else Modifier)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(labelRes).let { if(isRetro) it.uppercase() else it },
                        style = if (isRetro) MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RetroText)
                                else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isActive) {
                        Text(
                            text = if (personName.isNotEmpty()) stringResource(prefixRes, personName) else stringResource(noPersonRes),
                            style = if (isRetro) MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = RetroGold)
                                    else MaterialTheme.typography.bodyMedium,
                            color = if (isRetro) RetroGold else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { 
                        if (it) {
                            showEditDialog = true
                        } else {
                            onStatusChange(false, null)
                        }
                    },
                    colors = if (isRetro) SwitchDefaults.colors(
                        checkedThumbColor = RetroGold,
                        checkedTrackColor = RetroBlack,
                        uncheckedThumbColor = RetroGrey,
                        uncheckedTrackColor = RetroBlack
                    ) else SwitchDefaults.colors()
                )
            }
            
            if (isActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = if (isRetro) RectangleShape else ButtonDefaults.shape,
                    colors = if (isRetro) ButtonDefaults.buttonColors(containerColor = RetroBlue, contentColor = Color.White)
                             else ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                             )
                ) {
                    Text(
                        text = stringResource(R.string.edit_desc_button).let { if(isRetro) it.uppercase() else it },
                        style = if (isRetro) MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace) else LocalTextStyle.current
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            shape = if (isRetro) RectangleShape else AlertDialogDefaults.shape,
            containerColor = if (isRetro) RetroBackground else AlertDialogDefaults.containerColor,
            title = { 
                Text(
                    text = stringResource(dialogTitleRes).let { if(isRetro) it.uppercase() else it },
                    style = if (isRetro) MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else LocalTextStyle.current
                ) 
            },
            text = {
                OutlinedTextField(
                    value = tempPersonName,
                    onValueChange = { tempPersonName = it },
                    label = { Text(stringResource(R.string.name_hint), color = if(isRetro) RetroGold else Color.Unspecified) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = if (isRetro) RectangleShape else OutlinedTextFieldDefaults.shape,
                    textStyle = if (isRetro) MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else LocalTextStyle.current,
                    colors = if(isRetro) OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RetroGold,
                        unfocusedBorderColor = RetroText,
                        cursorColor = RetroGold
                    ) else OutlinedTextFieldDefaults.colors()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onStatusChange(true, tempPersonName)
                    showEditDialog = false
                }) {
                    Text(
                        text = stringResource(R.string.save_button).let { if(isRetro) it.uppercase() else it },
                        style = if (isRetro) MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RetroGold) else LocalTextStyle.current
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(
                        text = stringResource(R.string.cancel_button).let { if(isRetro) it.uppercase() else it },
                        style = if (isRetro) MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else LocalTextStyle.current
                    )
                }
            }
        )
    }
}

@Composable
fun NotesSection(
    notes: String,
    isRetro: Boolean,
    onNotesChange: (String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var tempNotes by remember(notes) { mutableStateOf(notes) }

    Surface(
        color = if (isRetro) RetroElementBackground else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = if (isRetro) RectangleShape else RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isRetro) Modifier.drawBehind { 
                drawRect(RetroBlack, style = Stroke(3.dp.toPx())) 
            } else Modifier)
            .clickable { showEditDialog = true }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.notes_label).let { if(isRetro) it.uppercase() else it },
                style = if (isRetro) MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RetroText)
                        else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (notes.isNotEmpty()) notes else stringResource(R.string.notes_hint).let { if(isRetro) it.uppercase() else it },
                style = if (isRetro) MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = if(notes.isNotEmpty()) RetroText else RetroText.copy(alpha = 0.5f))
                        else MaterialTheme.typography.bodyMedium,
                color = if (isRetro) (if(notes.isNotEmpty()) RetroText else RetroText.copy(alpha = 0.5f)) else (if(notes.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.4f)),
                maxLines = 5,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            shape = if (isRetro) RectangleShape else AlertDialogDefaults.shape,
            containerColor = if (isRetro) RetroBackground else AlertDialogDefaults.containerColor,
            title = { 
                Text(
                    text = stringResource(R.string.notes_dialog_title).let { if(isRetro) it.uppercase() else it },
                    style = if (isRetro) MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else LocalTextStyle.current
                ) 
            },
            text = {
                OutlinedTextField(
                    value = tempNotes,
                    onValueChange = { tempNotes = it },
                    label = { Text(stringResource(R.string.notes_hint), color = if(isRetro) RetroGold else Color.Unspecified) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    shape = if (isRetro) RectangleShape else OutlinedTextFieldDefaults.shape,
                    textStyle = if (isRetro) MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else LocalTextStyle.current,
                    colors = if(isRetro) OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RetroGold,
                        unfocusedBorderColor = RetroText,
                        cursorColor = RetroGold
                    ) else OutlinedTextFieldDefaults.colors()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onNotesChange(tempNotes)
                    showEditDialog = false
                }) {
                    Text(
                        text = stringResource(R.string.save_button).let { if(isRetro) it.uppercase() else it },
                        style = if (isRetro) MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RetroGold) else LocalTextStyle.current
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(
                        text = stringResource(R.string.cancel_button).let { if(isRetro) it.uppercase() else it },
                        style = if (isRetro) MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else LocalTextStyle.current
                    )
                }
            }
        )
    }
}

@Composable
fun MetadataChip(icon: String, label: String, isRetro: Boolean) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val labelStyle = if (isRetro) MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else MaterialTheme.typography.labelLarge
    
    Box(
        modifier = if (isRetro) Modifier
            .pixelButtonFrame(thickness = 2.dp)
            .padding(2.dp)
            .background(RetroElementBackground) 
        else Modifier
            .background(surfaceColor, RoundedCornerShape(12.dp))
            .drawBehind { drawRect(Color.White.copy(0.1f), style = Stroke(1.dp.toPx())) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label, 
                style = labelStyle, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CategoryChip(category: String, isRetro: Boolean) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val labelStyle = if (isRetro) MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = RetroText) else MaterialTheme.typography.labelSmall
    
    Box(
        modifier = if (isRetro) Modifier
            .pixelButtonFrame(thickness = 2.dp)
            .padding(2.dp)
            .background(RetroElementBackground)
        else Modifier
            .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .drawBehind { drawRect(primaryColor.copy(alpha = 0.2f), style = Stroke(1.dp.toPx())) }
    ) {
        Text(
            text = if (isRetro) category.uppercase() else category,
            style = labelStyle,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = if (isRetro) RetroText else primaryColor,
            fontWeight = FontWeight.Medium
        )
    }
}
