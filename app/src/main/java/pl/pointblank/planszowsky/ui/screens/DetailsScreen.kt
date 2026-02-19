package pl.pointblank.planszowsky.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
import pl.pointblank.planszowsky.util.decodeHtml
import androidx.compose.ui.platform.LocalUriHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val game by viewModel.game.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val isExpertChatEnabled by viewModel.isExpertChatEnabled.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()
    val translatedDescription by viewModel.translatedDescription.collectAsState()
    val isRetro = appTheme == AppTheme.PIXEL_ART
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    
    // Image Source Selection State
    var showSourcePicker by rememberSaveable { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val cameraPermissionState = com.google.accompanist.permissions.rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.updateLocalImage(context, it) }
    }

    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { viewModel.updateLocalImage(context, it) }
        }
    }

    fun createTempUri(): android.net.Uri {
        val file = java.io.File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    var showChat by remember { mutableStateOf(false) }

    if (showSourcePicker) {
        AlertDialog(
            onDismissRequest = { showSourcePicker = false },
            shape = if (isRetro) RectangleShape else RoundedCornerShape(28.dp),
            containerColor = if (isRetro) RetroBackground else MaterialTheme.colorScheme.surface,
            title = { 
                Text(
                    text = stringResource(R.string.select_source_title).let { if(isRetro) it.uppercase() else it },
                    style = if(isRetro) MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else MaterialTheme.typography.titleLarge
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SourceOption(
                        label = stringResource(R.string.source_camera),
                        icon = Icons.Default.PhotoCamera,
                        isRetro = isRetro,
                        onClick = {
                            showSourcePicker = false
                            if (cameraPermissionState.status.isGranted) {
                                val uri = createTempUri()
                                tempUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        }
                    )
                    SourceOption(
                        label = stringResource(R.string.source_gallery),
                        icon = Icons.Default.Image,
                        isRetro = isRetro,
                        onClick = {
                            showSourcePicker = false
                            photoPickerLauncher.launch("image/*")
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSourcePicker = false }) {
                    Text(
                        text = stringResource(R.string.cancel_button).let { if(isRetro) it.uppercase() else it },
                        color = if(isRetro) RetroText else MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }

    fun openGameWebsite(game: Game) {
        val url = game.websiteUrl ?: "https://www.google.com/search?q=${android.net.Uri.encode(game.title + " board game")}"
        try {
            uriHandler.openUri(url)
        } catch (e: Exception) {
            uriHandler.openUri("https://boardgamegeek.com/boardgame/${game.id}")
        }
    }

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
                        icon = { 
                            Box(modifier = Modifier.size(32.dp)) {
                                PixelChat24(color = RetroBlack) 
                            }
                        },
                        buttonSize = 64.dp,
                        shape = RoundedCornerShape(12.dp)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RetroSquareIconButton(onClick = onBackClick, color = RetroElementBackground) {
                        PixelBackIcon(color = RetroText)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            game?.let { g ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            bottom = paddingValues.calculateBottomPadding()
                        )
                        .then(if(isRetro) Modifier.padding(top = paddingValues.calculateTopPadding()) else Modifier)
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
                                .data(g.localImageUri ?: g.imageUrl)
                                .size(1024, 1024)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            filterQuality = if (isRetro) FilterQuality.None else FilterQuality.Low
                        )
                        
                        if (!g.isReadOnly) {
                            FilledIconButton(
                                onClick = { showSourcePicker = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if(isRetro) RetroGold else MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = if(isRetro) RetroBlack else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = stringResource(R.string.change_cover))
                            }
                        }
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
                                text = if (isRetro) g.title.decodeHtml().uppercase() else g.title.decodeHtml(),
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

                            GameManagementPanel(
                                game = g,
                                isRetro = isRetro,
                                onToggleFavorite = { viewModel.toggleFavorite() },
                                onToggleWishlist = { viewModel.toggleWishlist() },
                                onDelete = { 
                                    viewModel.deleteGame()
                                    onBackClick()
                                },
                                onUpdateBorrowingInfo = { isL, to, isF, from ->
                                    viewModel.updateBorrowingInfo(isL, to, isF, from)
                                },
                                onUpdateNotes = { viewModel.updateNotes(it) },
                                onWebsiteClick = { openGameWebsite(g) }
                            )

                            if (g.expansions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(32.dp))
                                ExpansionsSection(
                                    expansions = g.expansions,
                                    isReadOnly = g.isReadOnly,
                                    isRetro = isRetro,
                                    onToggle = { viewModel.toggleExpansion(it) }
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.description_label).let { if(isRetro) it.uppercase() else it },
                                    style = if (isRetro) MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RetroText)
                                            else MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                if (isRetro) {
                                    Box(
                                        modifier = Modifier
                                            .pixelButtonFrame(isSelected = translatedDescription != null, thickness = 2.dp)
                                            .background(if (translatedDescription != null) RetroBlue else RetroElementBackground)
                                            .clickable { viewModel.translateDescription() }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (isTranslating) stringResource(R.string.translating).uppercase()
                                                   else if (translatedDescription != null) stringResource(R.string.show_original).uppercase()
                                                   else stringResource(R.string.translate_button).uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                color = if(translatedDescription != null) Color.White else RetroText,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                } else {
                                    TextButton(
                                        onClick = { viewModel.translateDescription() },
                                        enabled = !isTranslating
                                    ) {
                                        if (isTranslating) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(stringResource(R.string.translating))
                                        } else {
                                            Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                if (translatedDescription != null) stringResource(R.string.show_original)
                                                else stringResource(R.string.translate_button)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = translatedDescription ?: g.description?.decodeHtml() ?: stringResource(R.string.no_description),
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
                            
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }

                if (!isRetro) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(16.dp)
                            .background(Color.Black.copy(0.3f), RoundedCornerShape(50))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back_button),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RetroDetailsTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RetroSquareIconButton(onClick = onBackClick, color = RetroElementBackground) {
            PixelBackIcon(color = RetroText)
        }
    }
}

@Composable
fun GameManagementPanel(
    game: Game,
    isRetro: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleWishlist: () -> Unit,
    onDelete: () -> Unit,
    onUpdateBorrowingInfo: (Boolean, String?, Boolean, String?) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onWebsiteClick: () -> Unit
) {
    if (isRetro) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pixelButtonFrame(thickness = 2.dp)
                .background(RetroElementBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RetroBlack)
                    .padding(vertical = 6.dp, horizontal = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.item_management),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = RetroGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    PixelActionButton(
                        icon = { PixelStar24(isSelected = game.isFavorite) },
                        label = stringResource(R.string.action_fav),
                        isActive = game.isFavorite,
                        activeColor = RetroGold,
                        onClick = onToggleFavorite
                    )
                    PixelActionButton(
                        icon = { PixelShinyHeart24(isSelected = game.isWishlisted) },
                        label = stringResource(R.string.action_wish),
                        isActive = game.isWishlisted,
                        activeColor = RetroRed,
                        onClick = onToggleWishlist
                    )
                    PixelActionButton(
                        icon = { PixelWeb24(color = RetroText) },
                        label = stringResource(R.string.action_www),
                        isActive = false,
                        activeColor = RetroBlue,
                        onClick = onWebsiteClick
                    )
                    if (!game.isReadOnly) {
                        PixelActionButton(
                            icon = { PixelDelete24(color = RetroText) },
                            label = stringResource(R.string.action_del),
                            isActive = false,
                            activeColor = RetroBlack,
                            onClick = onDelete
                        )
                    }
                }

                if (!game.isWishlisted && !game.isReadOnly) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(RetroBlack.copy(alpha = 0.3f)))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.borrowing_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = RetroText,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    var showBorrowDialog by remember { mutableStateOf(false) }
                    
                    val borrowText = when {
                        game.isBorrowed -> {
                            val name = if (!game.borrowedTo.isNullOrBlank()) game.borrowedTo else "..."
                            stringResource(R.string.type_lent) + ": " + name
                        }
                        game.isBorrowedFrom -> {
                            val name = if (!game.borrowedFrom.isNullOrBlank()) game.borrowedFrom else "..."
                            stringResource(R.string.type_borrowed) + ": " + name
                        }
                        else -> stringResource(R.string.borrowing_hint)
                    }
                    
                    val borrowColor = when {
                        game.isBorrowed -> RetroOrange
                        game.isBorrowedFrom -> RetroBlue
                        else -> Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .pixelButtonFrame(isSelected = game.isBorrowed || game.isBorrowedFrom, thickness = 2.dp)
                            .background(if(game.isBorrowed || game.isBorrowedFrom) borrowColor else Color.Transparent)
                            .clickable { showBorrowDialog = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if(game.isBorrowed || game.isBorrowedFrom) "[X] ${borrowText.uppercase()}" else "[ ] ${borrowText.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = if(game.isBorrowed || game.isBorrowedFrom) Color.White else RetroText,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }

                    if (showBorrowDialog) {
                        UnifiedBorrowingDialog(
                            isLent = game.isBorrowed,
                            isBorrowedFrom = game.isBorrowedFrom,
                            currentName = if(game.isBorrowed) game.borrowedTo ?: "" else game.borrowedFrom ?: "",
                            isRetro = true,
                            onDismiss = { showBorrowDialog = false },
                            onConfirm = { type, name ->
                                when(type) {
                                    BorrowType.LENT -> {
                                        onUpdateBorrowingInfo(true, name, false, null)
                                    }
                                    BorrowType.FROM -> {
                                        onUpdateBorrowingInfo(false, null, true, name)
                                    }
                                    BorrowType.NONE -> {
                                        onUpdateBorrowingInfo(false, null, false, null)
                                    }
                                }
                                showBorrowDialog = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(RetroBlack.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.notes_label).uppercase() + ":",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = RetroText,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                var showNotesDialog by remember { mutableStateOf(false) }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pixelButtonFrame(thickness = 1.dp)
                        .background(Color(0xFFFFF8E1))
                        .clickable { showNotesDialog = true }
                        .padding(12.dp)
                        .heightIn(min = 60.dp)
                ) {
                    Text(
                        text = if(game.notes?.isNotBlank() == true) game.notes else stringResource(R.string.tap_to_scribble),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = RetroBlack
                        )
                    )
                }

                if (showNotesDialog) {
                    EditNotesDialog(
                        initialNotes = game.notes ?: "",
                        isRetro = true,
                        onDismiss = { showNotesDialog = false },
                        onSave = onUpdateNotes
                    )
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ManagementIconButton(
                        icon = if (game.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        label = stringResource(R.string.action_fav),
                        isActive = game.isFavorite,
                        activeColor = RetroGold,
                        onClick = onToggleFavorite
                    )
                    ManagementIconButton(
                        icon = if (game.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        label = stringResource(R.string.action_wish),
                        isActive = game.isWishlisted,
                        activeColor = Color.Red,
                        onClick = onToggleWishlist
                    )
                    ManagementIconButton(
                        icon = Icons.Default.Public,
                        label = stringResource(R.string.action_www),
                        isActive = false,
                        activeColor = MaterialTheme.colorScheme.primary,
                        onClick = onWebsiteClick
                    )
                    if (!game.isReadOnly) {
                        ManagementIconButton(
                            icon = Icons.Default.Delete,
                            label = stringResource(R.string.action_del),
                            isActive = false,
                            activeColor = MaterialTheme.colorScheme.error,
                            onClick = onDelete
                        )
                    }
                }
                
                if (!game.isWishlisted && !game.isReadOnly) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    var showBorrowDialog by remember { mutableStateOf(false) }
                    
                    val borrowText = when {
                        game.isBorrowed -> stringResource(R.string.type_lent) + ": " + (game.borrowedTo ?: "")
                        game.isBorrowedFrom -> stringResource(R.string.type_borrowed) + ": " + (game.borrowedFrom ?: "")
                        else -> stringResource(R.string.borrowing_hint)
                    }

                    OutlinedCard(
                        onClick = { showBorrowDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SwapHoriz, null, Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(stringResource(R.string.borrowing_label), style = MaterialTheme.typography.labelMedium)
                                Text(borrowText, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    if (showBorrowDialog) {
                        UnifiedBorrowingDialog(
                            isLent = game.isBorrowed,
                            isBorrowedFrom = game.isBorrowedFrom,
                            currentName = if(game.isBorrowed) game.borrowedTo ?: "" else game.borrowedFrom ?: "",
                            isRetro = false,
                            onDismiss = { showBorrowDialog = false },
                            onConfirm = { type, name ->
                                when(type) {
                                    BorrowType.LENT -> {
                                        onUpdateBorrowingInfo(true, name, false, null)
                                    }
                                    BorrowType.FROM -> {
                                        onUpdateBorrowingInfo(false, null, true, name)
                                    }
                                    BorrowType.NONE -> {
                                        onUpdateBorrowingInfo(false, null, false, null)
                                    }
                                }
                                showBorrowDialog = false
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                var showNotesDialog by remember { mutableStateOf(false) }
                OutlinedCard(
                    onClick = { showNotesDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.notes_label), style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = if(game.notes?.isNotBlank() == true) game.notes  else stringResource(R.string.tap_to_scribble),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if(game.notes?.isNotBlank() == true) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (showNotesDialog) {
                    EditNotesDialog(
                        initialNotes = game.notes ?: "",
                        isRetro = false,
                        onDismiss = { showNotesDialog = false },
                        onSave = onUpdateNotes
                    )
                }
            }
        }
    }
}

enum class BorrowType { LENT, FROM, NONE }

@Composable
fun UnifiedBorrowingDialog(
    isLent: Boolean,
    isBorrowedFrom: Boolean,
    currentName: String,
    isRetro: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (BorrowType, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(if(isLent) BorrowType.LENT else if(isBorrowedFrom) BorrowType.FROM else BorrowType.NONE) }
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = if (isRetro) RectangleShape else RoundedCornerShape(28.dp),
        containerColor = if (isRetro) RetroBackground else MaterialTheme.colorScheme.surface,
        title = { Text(stringResource(R.string.borrow_dialog_unified_title).let { if(isRetro) it.uppercase() else it }, style = if(isRetro) MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(stringResource(R.string.borrow_type_label), style = MaterialTheme.typography.labelMedium, color = if(isRetro) RetroText else Color.Unspecified)
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BorrowTypeOption(BorrowType.NONE, stringResource(R.string.type_none), selectedType == BorrowType.NONE, isRetro) { selectedType = BorrowType.NONE }
                    BorrowTypeOption(BorrowType.LENT, stringResource(R.string.type_lent), selectedType == BorrowType.LENT, isRetro) { selectedType = BorrowType.LENT }
                    BorrowTypeOption(BorrowType.FROM, stringResource(R.string.type_borrowed), selectedType == BorrowType.FROM, isRetro) { selectedType = BorrowType.FROM }
                }

                if (selectedType != BorrowType.NONE) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.name_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = if(isRetro) RectangleShape else RoundedCornerShape(12.dp),
                        textStyle = if(isRetro) MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else LocalTextStyle.current,
                        colors = if(isRetro) OutlinedTextFieldDefaults.colors(focusedBorderColor = RetroGold, unfocusedBorderColor = RetroText) else OutlinedTextFieldDefaults.colors()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedType, name) }) {
                Text(stringResource(R.string.save_button).let { if(isRetro) it.uppercase() else it }, color = if(isRetro) RetroGold else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button).let { if(isRetro) it.uppercase() else it }, color = if(isRetro) RetroText else MaterialTheme.colorScheme.secondary)
            }
        }
    )
}

@Composable
fun BorrowTypeOption(type: BorrowType, label: String, isSelected: Boolean, isRetro: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick, colors = if(isRetro) RadioButtonDefaults.colors(selectedColor = RetroGold, unselectedColor = RetroText) else RadioButtonDefaults.colors())
        Spacer(modifier = Modifier.width(8.dp))
        Text(label.let { if(isRetro) it.uppercase() else it }, style = if(isRetro) MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = RetroText) else MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PixelActionButton(
    icon: @Composable () -> Unit,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .pixelButtonFrame(isSelected = isActive, thickness = 2.dp)
                .background(if (isActive) activeColor.copy(alpha = 0.2f) else Color.Transparent)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = RetroText,
                fontWeight = FontWeight.Bold
            )
        )
    }
}


@Composable
fun EditNotesDialog(
    initialNotes: String,
    isRetro: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var notes by remember { mutableStateOf(initialNotes) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = if (isRetro) RectangleShape else RoundedCornerShape(28.dp),
        containerColor = if (isRetro) RetroBackground else MaterialTheme.colorScheme.surface,
        title = { 
            Text(
                text = stringResource(R.string.notes_dialog_title).let { if(isRetro) it.uppercase() else it },
                style = if (isRetro) MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else MaterialTheme.typography.titleLarge
            ) 
        },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                shape = if (isRetro) RectangleShape else RoundedCornerShape(12.dp),
                textStyle = if (isRetro) MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else MaterialTheme.typography.bodyLarge,
                colors = if(isRetro) OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RetroGold,
                    unfocusedBorderColor = RetroText,
                    cursorColor = RetroGold
                ) else OutlinedTextFieldDefaults.colors()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(notes); onDismiss() }) {
                Text(
                    text = stringResource(R.string.save_button).let { if(isRetro) it.uppercase() else it },
                    style = if (isRetro) MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RetroGold) else MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel_button).let { if(isRetro) it.uppercase() else it },
                    style = if (isRetro) MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

@Composable
fun ManagementIconButton(icon: ImageVector, label: String, isActive: Boolean, activeColor: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.background(if (isActive) activeColor.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(12.dp))
        ) {
            Icon(icon, contentDescription = label, tint = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ExpansionsSection(
    expansions: List<pl.pointblank.planszowsky.domain.model.Expansion>,
    isReadOnly: Boolean,
    isRetro: Boolean,
    onToggle: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.expansions_label).let { if (isRetro) it.uppercase() else it },
            style = if (isRetro) MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RetroText)
                    else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = if (isRetro) Modifier
                .pixelButtonFrame(thickness = 2.dp)
                .background(RetroElementBackground)
                .padding(8.dp)
            else Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            expansions.forEach { expansion ->
                ExpansionItem(
                    expansion = expansion,
                    isReadOnly = isReadOnly,
                    isRetro = isRetro,
                    onClick = { if (!isReadOnly) onToggle(expansion.id) }
                )
            }
        }
    }
}

@Composable
fun ExpansionItem(
    expansion: pl.pointblank.planszowsky.domain.model.Expansion,
    isReadOnly: Boolean,
    isRetro: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isReadOnly, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRetro) {
            Text(
                text = if (expansion.isOwned) "[X]" else "[ ]",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = if(expansion.isOwned) RetroGold else RetroText),
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = expansion.title.uppercase(),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = RetroText),
                modifier = Modifier.weight(1f)
            )
        } else {
            Checkbox(
                checked = expansion.isOwned,
                onCheckedChange = { if(!isReadOnly) onClick() },
                enabled = !isReadOnly,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = expansion.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SourceOption(
    label: String,
    icon: ImageVector,
    isRetro: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = if (isRetro) RectangleShape else RoundedCornerShape(12.dp),
        color = if (isRetro) RetroElementBackground else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().then(if(isRetro) Modifier.drawBehind { drawRect(RetroBlack, style = Stroke(2.dp.toPx())) } else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isRetro) RetroGold else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = if (isRetro) label.uppercase() else label,
                style = if (isRetro) MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText) else MaterialTheme.typography.bodyLarge
            )
        }
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