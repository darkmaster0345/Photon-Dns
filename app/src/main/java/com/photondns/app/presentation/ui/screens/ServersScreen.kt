package com.photondns.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.photondns.app.data.models.DNSProtocol
import com.photondns.app.presentation.ui.components.DNSServerCard
import com.photondns.app.presentation.viewmodel.ServersViewModel

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
        Text(
            text = "SERVERS",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Color(0xFF00E5CC)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                viewModel.searchServers(it)
            },
            placeholder = { Text("Search nodes...", color = Color.White.copy(alpha = 0.3f)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF00E5CC)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF131313),
                unfocusedContainerColor = Color(0xFF131313),
                focusedBorderColor = Color(0xFF00E5CC).copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.05f)
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val serversToDisplay = if (searchQuery.isBlank()) uiState.servers else uiState.filteredServers
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(serversToDisplay) { server ->
                DNSServerCard(
                    server = server,
                    isActive = server.id == uiState.servers.find { it.isActive }?.id,
                    onServerClick = { viewModel.switchToServer(it) }
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color(0xFF00E5CC),
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, null)
        }
    }
    
    if (showAddDialog) {
        AddServerDialog(
            onDismiss = { showAddDialog = false },
            onAddServer = { name, ip, countryCode, protocol, dohUrl, dotHostname ->
                viewModel.addCustomServer(name, ip, countryCode, protocol, dohUrl, dotHostname)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServerDialog(
    onDismiss: () -> Unit,
    onAddServer: (String, String, String, DNSProtocol, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf(DNSProtocol.UDP) }
    var dohUrl by remember { mutableStateOf("") }
    var dotHostname by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Node", color = Color(0xFF00E5CC)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ip, onValueChange = { ip = it }, label = { Text("IP/Endpoint") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = countryCode, onValueChange = { countryCode = it }, label = { Text("Country Code (US, SE, etc)") }, modifier = Modifier.fillMaxWidth())

                Text("Protocol", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.selectableGroup()) {
                    DNSProtocol.values().forEach { text ->
                        Row(
                            Modifier.padding(horizontal = 4.dp).selectable(
                                selected = (text == protocol),
                                onClick = { protocol = text },
                                role = Role.RadioButton
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (text == protocol), onClick = null)
                            Text(text = text.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }

                if (protocol == DNSProtocol.DOH) {
                    OutlinedTextField(value = dohUrl, onValueChange = { dohUrl = it }, label = { Text("DoH URL") }, modifier = Modifier.fillMaxWidth())
                }
                if (protocol == DNSProtocol.DOT) {
                    OutlinedTextField(value = dotHostname, onValueChange = { dotHostname = it }, label = { Text("DoT Hostname") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddServer(name, ip, countryCode, protocol, dohUrl, dotHostname) },
                enabled = name.isNotBlank() && ip.isNotBlank() && countryCode.isNotBlank() &&
                    (protocol != DNSProtocol.DOH || dohUrl.isNotBlank()) &&
                    (protocol != DNSProtocol.DOT || dotHostname.isNotBlank())
            ) { Text("Save Node") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = Color(0xFF131313)
    )
}
