package com.photondns.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.photondns.app.presentation.navigation.PhotonNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            PhotonDnsApp()
        }
    }
}

@Composable
fun PhotonDnsApp() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF00E5CC), // Cyan
            secondary = Color(0xFF00D9A3), // Accent green
            background = Color(0xFF0A0A0A), // AMOLED dark
            surface = Color(0xFF1A1A1A),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PhotonNavigation()
        }
    }
}
