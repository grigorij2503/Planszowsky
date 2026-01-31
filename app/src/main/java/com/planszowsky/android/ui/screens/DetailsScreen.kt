package com.planszowsky.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.planszowsky.android.domain.model.Game
import com.planszowsky.android.ui.viewmodel.DetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val game by viewModel.game.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleWishlist() },
                        modifier = Modifier.background(Color.Black.copy(0.3f), RoundedCornerShape(50))
                    ) {
                        Icon(
                            imageVector = if (game?.isWishlisted == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (game?.isWishlisted == true) Color.Red else Color.White
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
                        Icon(Icons.Default.Delete, contentDescription = "Usuń")
                    }
                }
            )
        }
    ) { padding ->
        game?.let { g ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Image (Parallax Placeholder)
                Box(modifier = Modifier.height(400.dp).fillMaxWidth()) {
                    AsyncImage(
                        model = g.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-32).dp),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = g.title,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = g.yearPublished ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Chips row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetadataChip(icon = "👥", label = "${g.minPlayers}-${g.maxPlayers}")
                            MetadataChip(icon = "⏳", label = "${g.playingTime}m")
                            MetadataChip(icon = "🧠", label = "3.5/5")
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        BorrowingSection(
                            isBorrowed = g.isBorrowed,
                            borrowedTo = g.borrowedTo ?: "",
                            onStatusChange = { isBorrowed, borrowedTo ->
                                viewModel.updateBorrowedStatus(isBorrowed, borrowedTo)
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = "Opis",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = g.description ?: "Brak opisu.",
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(100.dp)) // Extra space
                    }
                }
            }
        }
    }
}

@Composable
fun BorrowingSection(
    isBorrowed: Boolean,
    borrowedTo: String,
    onStatusChange: (Boolean, String?) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var tempBorrowedTo by remember(borrowedTo) { mutableStateOf(borrowedTo) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Pożyczona",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isBorrowed) {
                        Text(
                            text = if (borrowedTo.isNotEmpty()) "Komu: $borrowedTo" else "Brak opisu komu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Switch(
                    checked = isBorrowed,
                    onCheckedChange = { 
                        if (it) {
                            showEditDialog = true
                        } else {
                            onStatusChange(false, null)
                        }
                    }
                )
            }
            
            if (isBorrowed) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Edytuj opis")
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Komu pożyczyłeś grę?") },
            text = {
                OutlinedTextField(
                    value = tempBorrowedTo,
                    onValueChange = { tempBorrowedTo = it },
                    label = { Text("Imię / Opis") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onStatusChange(true, tempBorrowedTo)
                    showEditDialog = false
                }) {
                    Text("Zapisz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun MetadataChip(icon: String, label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}
