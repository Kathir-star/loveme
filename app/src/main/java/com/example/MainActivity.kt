package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.StyleRepository
import com.example.ui.screens.StyleStudioApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StyleViewModel
import com.example.ui.viewmodel.StyleViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize Room Database and Repository
    val database = AppDatabase.getDatabase(this)
    val repository = StyleRepository(database.styleDao())
    
    // Instantiate StyleViewModel using factory
    val factory = StyleViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, factory)[StyleViewModel::class.java]

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        StyleStudioApp(viewModel = viewModel)
      }
    }
  }
}
