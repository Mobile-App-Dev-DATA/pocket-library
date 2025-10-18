package com.example.pocket_library

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.pocket_library.ui.theme.PocketlibraryTheme

class MainActivity : ComponentActivity() {
    private val viewModel: LibraryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        APIImpl.initialize(this)
        enableEdgeToEdge()
        setContent {
            PocketlibraryTheme {
                AppNavigation(viewModel)
            }
        }
    }
}