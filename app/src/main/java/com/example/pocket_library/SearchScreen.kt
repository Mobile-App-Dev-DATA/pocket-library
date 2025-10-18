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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen(viewModel: LibraryViewModel) {
    // ✅ Lecture pattern: Single collectAsState at top
    val state by viewModel.state.collectAsState()

    // ✅ Lecture pattern: LocalConfiguration for responsive UI
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    var showManualEntry by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<Book?>(null) }

    if (isTablet) {
        // Tablet split layout
        Row(modifier = Modifier.fillMaxSize()) {
            // Left: List (40%)
            Column(modifier = Modifier.weight(0.4f)) {
                SearchInputs(state, viewModel, onManualEntry = { showManualEntry = true })
                Divider()
                SearchResultsList(
                    results = state.searchResults,
                    favourites = state.favList,
                    viewModel = viewModel,
                    scrollIndex = state.searchScrollIndex,
                    onScrollIndexChange = { viewModel.updateSearchScrollPosition(it) },
                    onBookClick = { selectedBook = it }
                )
            }

            VerticalDivider()

            // Right: Details (60%)
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(16.dp)
            ) {
                if (selectedBook != null) {
                    BookDetails(
                        book = selectedBook!!,
                        isFavourite = state.favList.any { it.id == selectedBook!!.id },
                        onToggleFavourite = {
                            if (state.favList.any { it.id == selectedBook!!.id }) {
                                viewModel.removeFavourite(selectedBook!!)
                            } else {
                                viewModel.addFavourite(selectedBook!!)
                            }
                        }
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
            SearchInputs(state, viewModel, onManualEntry = { showManualEntry = true })

            if (state.searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Search for books to see results")
                }
            } else {
                if (isLandscape) {
                    // Landscape: 2-column grid
                    val gridState = rememberLazyGridState()

                    LaunchedEffect(state.searchScrollIndex) {
                        if (state.searchScrollIndex > 0 && state.searchResults.isNotEmpty()) {
                            gridState.scrollToItem(state.searchScrollIndex)
                        }
                    }

                    LaunchedEffect(gridState.firstVisibleItemIndex) {
                        viewModel.updateSearchScrollPosition(gridState.firstVisibleItemIndex)
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.searchResults, key = { it.id }) { book ->
                            BookCard(
                                book = book,
                                isFavourite = state.favList.any { it.id == book.id },
                                onFavouriteToggle = {
                                    if (state.favList.any { it.id == book.id }) {
                                        viewModel.removeFavourite(book)
                                    } else {
                                        viewModel.addFavourite(book)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    // Portrait: Single column
                    val listState = rememberLazyListState()

                    LaunchedEffect(state.searchScrollIndex) {
                        if (state.searchScrollIndex > 0 && state.searchResults.isNotEmpty()) {
                            listState.scrollToItem(state.searchScrollIndex)
                        }
                    }

                    LaunchedEffect(listState.firstVisibleItemIndex) {
                        viewModel.updateSearchScrollPosition(listState.firstVisibleItemIndex)
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.searchResults, key = { it.id }) { book ->
                            BookCard(
                                book = book,
                                isFavourite = state.favList.any { it.id == book.id },
                                onFavouriteToggle = {
                                    if (state.favList.any { it.id == book.id }) {
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
                    coverImg = null,
                    myPicture = null
                )
                viewModel.addFavourite(manualBook)
                showManualEntry = false
            }
        )
    }
}

@Composable
fun SearchInputs(
    state: LibraryViewModel.State,
    viewModel: LibraryViewModel,
    onManualEntry: () -> Unit
) {
    Column {
        TextField(
            value = state.search.title,
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
                value = state.search.author,
                onValueChange = { viewModel.setSearch(author = it) },
                modifier = Modifier.weight(1f),
                label = { Text("Author") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = state.search.year ?: "",
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
                onClick = onManualEntry,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Manually")
            }
        }
    }
}

@Composable
fun SearchResultsList(
    results: List<Book>,
    favourites: List<Book>,
    viewModel: LibraryViewModel,
    scrollIndex: Int,
    onScrollIndexChange: (Int) -> Unit,
    onBookClick: (Book) -> Unit
) {
    if (results.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Search for books")
        }
    } else {
        val listState = rememberLazyListState()

        LaunchedEffect(scrollIndex) {
            if (scrollIndex > 0 && results.isNotEmpty()) {
                listState.scrollToItem(scrollIndex)
            }
        }

        LaunchedEffect(listState.firstVisibleItemIndex) {
            onScrollIndexChange(listState.firstVisibleItemIndex)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results, key = { it.id }) { book ->
                BookCard(
                    book = book,
                    isFavourite = favourites.any { it.id == book.id },
                    onFavouriteToggle = {
                        if (favourites.any { it.id == book.id }) {
                            viewModel.removeFavourite(book)
                        } else {
                            viewModel.addFavourite(book)
                        }
                    },
                    onClick = { onBookClick(book) }
                )
            }
        }
    }
}

@Composable
fun BookCard(
    book: Book,
    isFavourite: Boolean,
    onFavouriteToggle: () -> Unit,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
fun BookDetails(
    book: Book,
    isFavourite: Boolean,
    onToggleFavourite: () -> Unit
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
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Published: ${book.year}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onToggleFavourite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isFavourite) "Remove from Library" else "Add to Library")
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