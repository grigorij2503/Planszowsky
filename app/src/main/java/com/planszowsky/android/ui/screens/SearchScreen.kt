package com.planszowsky.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planszowsky.android.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val results by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szukaj gry") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TextField(
                value = query,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = { Text("Wpisz tytuł gry...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    )
                }
                
                LazyColumn {
                    items(results) { game ->
                        ListItem(
                            headlineContent = { Text(game.title) },
                            supportingContent = { Text(game.yearPublished ?: "") },
                            modifier = Modifier.clickable { 
                                viewModel.addToCollection(game)
                                onBackClick() // Wróć do listy kolekcji po dodaniu
                            }
                        )
                    }
                }
            }
        }
    }
}
