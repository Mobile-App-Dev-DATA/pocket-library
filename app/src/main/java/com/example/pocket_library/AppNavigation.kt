package com.example.pocket_library

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

enum class AppScreen {
    SEARCH, MY_LIBRARY
}

@Composable
fun AppNavigation(viewModel: LibraryViewModel) {
    var selectedScreen by remember { mutableStateOf(AppScreen.SEARCH) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    label = { Text("Search") },
                    selected = selectedScreen == AppScreen.SEARCH,
                    onClick = { selectedScreen = AppScreen.SEARCH }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "My Library") },
                    label = { Text("My Library") },
                    selected = selectedScreen == AppScreen.MY_LIBRARY,
                    onClick = { selectedScreen = AppScreen.MY_LIBRARY }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedScreen) {
                AppScreen.SEARCH -> SearchScreen(viewModel)
                AppScreen.MY_LIBRARY -> MyLibraryScreen(viewModel)
            }
        }
    }
}