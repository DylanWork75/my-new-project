package com.example.workapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.workapp.data.repository.CityRepositoryImpl
import com.example.workapp.ui.map.CityMapScreen
import com.example.workapp.ui.map.MapViewModel
import com.example.workapp.ui.theme.WorkAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkAppTheme {
                val repository = remember { CityRepositoryImpl() }
                val viewModel = remember { MapViewModel(repository) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CityMapScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
