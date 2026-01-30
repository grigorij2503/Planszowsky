package com.planszowsky.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.planszowsky.android.domain.model.Game
import com.planszowsky.android.ui.viewmodel.CollectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel = hiltViewModel(),
    onAddGameClick: () -> Unit,
    onGameClick: (String) -> Unit
) {
    val games by viewModel.games.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Moja Kolekcja") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGameClick) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj grę")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(games) { game ->
                GameItem(game, onClick = { onGameClick(game.id) })
            }
        }
    }
}

@Composable
fun GameItem(game: Game, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = game.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = game.title, style = MaterialTheme.typography.titleMedium)
                game.yearPublished?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}