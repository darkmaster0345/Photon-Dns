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
            PhotonDnsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PhotonNavigation()
                }
            }
        }
    }
}

@Composable
fun PhotonDnsTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = Color(0xFF00E5CC), // Cyan
        onPrimary = Color(0xFF003730),
        primaryContainer = Color(0xFF006256),
        onPrimaryContainer = Color(0xFF71FFE8),
        secondary = Color(0xFF00D9A3), // Accent green
        onSecondary = Color(0xFF003828),
        secondaryContainer = Color(0xFF005840),
        onSecondaryContainer = Color(0xFF44F5BD),
        background = Color(0xFF0A0A0A), // True AMOLED Black
        onBackground = Color(0xFFE5E2E1),
        surface = Color(0xFF131313), // Deep Surface
        onSurface = Color(0xFFE5E2E1),
        surfaceVariant = Color(0xFF1A1A1A),
        onSurfaceVariant = Color(0xFFB9CAC5)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
