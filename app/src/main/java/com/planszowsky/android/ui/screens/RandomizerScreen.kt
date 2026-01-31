package com.planszowsky.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.planszowsky.android.R
import com.planszowsky.android.ui.viewmodel.RandomizerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomizerScreen(
    viewModel: RandomizerViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val selectedGame by viewModel.selectedGame.collectAsState()
    val isSpinning by viewModel.isSpinning.collectAsState()
    val games by viewModel.games.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.randomizer_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.cant_decide),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Slot Machine Area
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSpinning) {
                    // Spinning animation placeholder
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else if (selectedGame != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = selectedGame?.imageUrl ?: selectedGame?.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = selectedGame?.title ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Text(text = "🎰", fontSize = 64.sp)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.spin() },
                enabled = !isSpinning && games.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(64.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isSpinning) stringResource(R.string.spinning) else stringResource(R.string.spin_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (games.isEmpty()) {
                Text(
                    text = stringResource(R.string.empty_collection_warning),
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
