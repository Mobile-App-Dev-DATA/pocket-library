package com.example.pocket_library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MyLibraryScreen(viewModel: LibraryViewModel) {
    val state by viewModel.state.collectAsState()
    val favourites = state.favList

    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Restore scroll position
    LaunchedEffect(state.libraryScrollIndex) {
        if (state.libraryScrollIndex > 0 && favourites.isNotEmpty()) {
            listState.scrollToItem(state.libraryScrollIndex)
        }
    }

    // Save scroll position
    LaunchedEffect(listState.firstVisibleItemIndex) {
        viewModel.updateLibraryScrollPosition(listState.firstVisibleItemIndex)
    }

    val filteredFavourites = remember(favourites, searchQuery) {
        if (searchQuery.isBlank()) {
            favourites
        } else {
            favourites.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.author.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("Search your library") }
        )

        Text(
            text = "${filteredFavourites.size} book(s) in your library",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredFavourites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No books in your library yet")
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredFavourites, key = { it.id }) { book ->
                    LibraryBookCard(
                        book = book,
                        onRemove = { viewModel.removeFavourite(book) },
                        onAddPhoto = { viewModel.addPersonalPhotoToFavourite(book) },
                        onShare = {
                            // TODO: Implement contact picker
                            viewModel.shareBook(book, "contact@example.com")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryBookCard(
    book: Book,
    onRemove: () -> Unit,
    onAddPhoto: () -> Unit,
    onShare: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onAddPhoto,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Add photo",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Photo")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onRemove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove")
                    }
                }
            }
        }
    }
}