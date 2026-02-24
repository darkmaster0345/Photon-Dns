package com.photondns.app.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.photondns.app.presentation.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotonNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0A0A0A),
                contentColor = Color(0xFF00E5CC)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E5CC),
                            selectedTextColor = Color(0xFF00E5CC),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF1A1A1A)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.Monitor.route) {
                MonitorScreen()
            }
            composable(Screen.SpeedTest.route) {
                SpeedTestScreen()
            }
            composable(Screen.Servers.route) {
                ServersScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Monitor : Screen("monitor", "Monitor", Icons.Default.NetworkCheck)
    object SpeedTest : Screen("speed_test", "Speed", Icons.Default.Speed)
    object Servers : Screen("servers", "Servers", Icons.Default.Dns)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

private val bottomNavItems = listOf(
    Screen.Home,
    Screen.Monitor,
    Screen.SpeedTest,
    Screen.Servers,
    Screen.Settings
)
