package com.example.pocket_library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen(viewModel: LibraryViewModel) {
    val state by viewModel.state.collectAsState()
    val searchQuery = state.search
    val searchResults = state.searchResults
    val favourites = state.favList

    var showManualEntry by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Restore scroll position
    LaunchedEffect(state.searchScrollIndex) {
        if (state.searchScrollIndex > 0 && searchResults.isNotEmpty()) {
            listState.scrollToItem(state.searchScrollIndex)
        }
    }

    // Save scroll position
    LaunchedEffect(listState.firstVisibleItemIndex) {
        viewModel.updateSearchScrollPosition(listState.firstVisibleItemIndex)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = searchQuery.title,
            onValueChange = { viewModel.setSearch(title = it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("Search books by title") },
            trailingIcon = {
                IconButton(onClick = { viewModel.performSearch() }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = searchQuery.author,
                onValueChange = { viewModel.setSearch(author = it) },
                modifier = Modifier.weight(1f),
                label = { Text("Author") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = searchQuery.year ?: "",
                onValueChange = { viewModel.setSearch(year = it) },
                modifier = Modifier.weight(1f),
                label = { Text("Year") }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { viewModel.performSearch() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { showManualEntry = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Manually")
            }
        }

        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Search for books to see results")
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults, key = { it.id }) { book ->
                    BookCard(
                        book = book,
                        isFavourite = favourites.any { it.id == book.id },
                        onFavouriteToggle = {
                            if (favourites.any { it.id == book.id }) {
                                viewModel.removeFavourite(book)
                            } else {
                                viewModel.addFavourite(book)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showManualEntry) {
        ManualEntryDialog(
            onDismiss = { showManualEntry = false },
            onSave = { title, author, year ->
                val manualBook = Book(
                    id = "manual_${System.currentTimeMillis()}",
                    title = title,
                    author = author,
                    year = year,
                    coverUrl = null,
                    myPicture = null
                )
                viewModel.addFavourite(manualBook)
                showManualEntry = false
            }
        )
    }
}

@Composable
fun BookCard(
    book: Book,
    isFavourite: Boolean,
    onFavouriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Show book details */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
                // --- Book cover on the left ---
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = "Book cover for ${book.title}",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(end = 16.dp)
                )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = book.year,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onFavouriteToggle) {
                Icon(
                    imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                    tint = if (isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ManualEntryDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Book Manually") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )
                TextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") }
                )
                TextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, author, year) },
                enabled = title.isNotBlank() && author.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}