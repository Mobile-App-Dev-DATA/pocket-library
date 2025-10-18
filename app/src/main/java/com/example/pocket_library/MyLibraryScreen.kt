package com.example.pocket_library

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun MyLibraryScreen(viewModel: LibraryViewModel) {
    val state by viewModel.state.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    var searchQuery by remember { mutableStateOf("") }
    var selectedBook by remember { mutableStateOf<Book?>(null) }

    val filteredFavourites = remember(state.favList, searchQuery) {
        if (searchQuery.isBlank()) {
            state.favList
        } else {
            state.favList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.author.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    if (isTablet) {
        // Tablet split layout
        Row(modifier = Modifier.fillMaxSize()) {
            // Left: List (40%)
            Column(modifier = Modifier.weight(0.4f)) {
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
                    val listState = rememberLazyListState()

                    LaunchedEffect(state.libraryScrollIndex) {
                        if (state.libraryScrollIndex > 0 && filteredFavourites.isNotEmpty()) {
                            listState.scrollToItem(state.libraryScrollIndex)
                        }
                    }

                    LaunchedEffect(listState.firstVisibleItemIndex) {
                        viewModel.updateLibraryScrollPosition(listState.firstVisibleItemIndex)
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFavourites, key = { it.id }) { book ->
                            LibraryBookCard(
                                book = book,
                                isSelected = selectedBook?.id == book.id,
                                onClick = { selectedBook = book },
                                compact = true
                            )
                        }
                    }
                }
            }

            VerticalDivider()

            // Right: Details (60%)
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(16.dp)
            ) {
                if (selectedBook != null) {
                    LibraryBookDetails(
                        book = selectedBook!!,
                        viewModel = viewModel
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Select a book to view details")
                    }
                }
            }
        }
    } else {
        // Phone layout
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
                if (isLandscape) {
                    // Landscape: 2-column grid
                    val gridState = rememberLazyGridState()

                    LaunchedEffect(state.libraryScrollIndex) {
                        if (state.libraryScrollIndex > 0 && filteredFavourites.isNotEmpty()) {
                            gridState.scrollToItem(state.libraryScrollIndex)
                        }
                    }

                    LaunchedEffect(gridState.firstVisibleItemIndex) {
                        viewModel.updateLibraryScrollPosition(gridState.firstVisibleItemIndex)
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFavourites, key = { it.id }) { book ->
                            LibraryBookCard(
                                book = book,
                                viewModel = viewModel
                            )
                        }
                    }
                } else {
                    // Portrait: Single column
                    val listState = rememberLazyListState()

                    LaunchedEffect(state.libraryScrollIndex) {
                        if (state.libraryScrollIndex > 0 && filteredFavourites.isNotEmpty()) {
                            listState.scrollToItem(state.libraryScrollIndex)
                        }
                    }

                    LaunchedEffect(listState.firstVisibleItemIndex) {
                        viewModel.updateLibraryScrollPosition(listState.firstVisibleItemIndex)
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFavourites, key = { it.id }) { book ->
                            LibraryBookCard(
                                book = book,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryBookCard(
    book: Book,
    viewModel: LibraryViewModel? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    compact: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (compact) {
                    onClick()
                } else {
                    expanded = !expanded
                }
            },
        colors = if (isSelected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

            if (expanded && !compact && viewModel != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { viewModel.addPersonalPhotoToFavourite(book) },
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
                        onClick = { viewModel.shareBook(book, "contact@example.com") },
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
                        onClick = { viewModel.removeFavourite(book) },
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

@Composable
fun LibraryBookDetails(
    book: Book,
    viewModel: LibraryViewModel
) {
    Card(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "by ${book.author}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Published: ${book.year}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.addPersonalPhotoToFavourite(book) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Photo")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.shareBook(book, "contact@example.com") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Book")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.removeFavourite(book) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Remove from Library")
            }
        }
    }
}