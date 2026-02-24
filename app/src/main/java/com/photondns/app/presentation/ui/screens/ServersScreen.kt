package com.photondns.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.photondns.app.presentation.ui.components.DNSServerCard
import com.photondns.app.presentation.viewmodel.ServersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    viewModel: ServersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DNS Servers",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5CC)
            )
            
            Row {
                IconButton(
                    onClick = { viewModel.refreshLatency() },
                    enabled = !uiState.isRefreshing
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = if (uiState.isRefreshing) Color.Gray else Color(0xFF00E5CC)
                    )
                }
                
                IconButton(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Server",
                        tint = Color(0xFF00E5CC)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                viewModel.searchServers(it)
            },
            placeholder = { Text("Search servers...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5CC),
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Fastest servers section
        val fastestServers = viewModel.getFastestServers()
        if (fastestServers.isNotEmpty()) {
            Text(
                text = "Fastest Servers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(fastestServers) { server ->
                    DNSServerCard(
                        server = server,
                        isActive = server.id == uiState.servers.find { it.isActive }?.id,
                        isFastest = true,
                        onServerClick = { viewModel.switchToServer(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // All servers section
        Text(
            text = "All Servers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val serversToDisplay = if (searchQuery.isBlank()) {
            uiState.servers
        } else {
            uiState.filteredServers
        }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(serversToDisplay) { server ->
                DNSServerCard(
                    server = server,
                    isActive = server.id == uiState.servers.find { it.isActive }?.id,
                    isFastest = fastestServers.any { it.id == server.id },
                    onServerClick = { viewModel.switchToServer(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Error handling
        uiState.error?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF4444).copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    color = Color(0xFFFF4444),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
    
    // Add server dialog
    if (showAddDialog) {
        AddServerDialog(
            onDismiss = { showAddDialog = false },
            onAddServer = { name, ip, countryCode ->
                viewModel.addCustomServer(name, ip, countryCode)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServerDialog(
    onDismiss: () -> Unit,
    onAddServer: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Custom DNS Server",
                color = Color(0xFF00E5CC)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Server Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5CC)
                    )
                )
                
                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("IP Address") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5CC)
                    )
                )
                
                OutlinedTextField(
                    value = countryCode,
                    onValueChange = { countryCode = it },
                    label = { Text("Country Code (e.g., US)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5CC)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && ip.isNotBlank() && countryCode.isNotBlank()) {
                        onAddServer(name.trim(), ip.trim(), countryCode.trim().uppercase())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5CC)
                ),
                enabled = name.isNotBlank() && ip.isNotBlank() && countryCode.isNotBlank()
            ) {
                Text(
                    text = "Add Server",
                    color = Color.Black
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = Color(0xFF00E5CC)
                )
            }
        },
        containerColor = Color(0xFF1A1A1A)
    )
}
