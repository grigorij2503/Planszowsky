package com.planszowsky.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.planszowsky.android.R
import com.planszowsky.android.ui.viewmodel.SearchViewModel

import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    initialQuery: String? = null,
    onBackClick: () -> Unit
) {
    val results by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    // Trigger initial search if query is passed from scanner
    LaunchedEffect(initialQuery) {
        if (initialQuery != null) {
            viewModel.onQueryChange(initialQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TextField(
                value = query,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = { Text(stringResource(R.string.search_field_hint)) },
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
                            leadingContent = {
                                AsyncImage(
                                    model = "https://picsum.photos/id/10/100/100.jpg", // Testowy obrazek bezpośrednio
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp)
                                )
                            },
                            modifier = Modifier.clickable { 
                                viewModel.addToCollection(game)
                                onBackClick()
                            }
                        )
                    }
                }
            }
        }
    }
}
