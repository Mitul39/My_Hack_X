package com.mtl.My_Hack_X.ui.screens.eventdetails

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.mtl.My_Hack_X.data.models.HackathonEvent
import com.mtl.My_Hack_X.data.models.User
import com.mtl.My_Hack_X.ui.components.ErrorMessage
import com.mtl.My_Hack_X.ui.components.LoadingSpinner
import com.mtl.My_Hack_X.ui.components.MainTopAppBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    navController: NavController,
    viewModel: EventDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(eventId) {
        viewModel.loadEventDetails(eventId)
    }
    
    Scaffold(
        topBar = {
            MainTopAppBar(
                title = uiState.event?.name ?: "Event Details",
                canNavigateBack = true,
                navigateUp = { navController.navigateUp() },
                modifier = Modifier
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    LoadingSpinner()
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadEventDetails(eventId) }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.event != null -> {
                    val event = uiState.event!!
                    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Event Description
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Event Details
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Event Details",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${dateFormat.format(event.startDate)} - ${dateFormat.format(event.endDate)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = event.location,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Team Size: ${event.minTeamSize} - ${event.maxTeamSize} members",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Status: ${event.status}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Tags
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Tags",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    event.tags.forEach { tag ->
                                        SuggestionChip(
                                            onClick = { },
                                            label = { Text(tag) }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Prizes
                        if (event.prizes.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Prizes",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    event.prizes.forEach { prize ->
                                        Text(
                                            text = "• $prize",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Registration button
                        Button(
                            onClick = { viewModel.registerForEvent() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Register for Event")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetailsContent(event: HackathonEvent) {
    val scrollState = rememberScrollState()
    val currentUser = FirebaseAuth.getInstance().currentUser?.let {
        User(
            uid = it.uid,
            email = it.email ?: "",
            displayName = it.displayName ?: "",
            photoUrl = it.photoUrl?.toString() ?: ""
        )
    }
    
    // Check if current user is registered for this event
    val isUserRegistered = currentUser?.let { user ->
        // First try to check in teamObjects
        val teamObjects = event.getTeamObjectsSafely()
        if (teamObjects.isNotEmpty()) {
            teamObjects.any { team ->
                team.members.any { it.uid == user.uid } || team.leaderId == user.uid
            }
        } else {
            // Fall back to comparing with team IDs if teamObjects is empty
            false
        }
    } ?: false
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Event name
        Text(
            text = event.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Event details
        Text(
            text = "Location: ${event.location}",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "Date: ${formatDate(event.startDate)} - ${formatDate(event.endDate)}",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "Registration Deadline: ${formatDate(event.registrationDeadline)}",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "Status: ${event.status}",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tags
        val tags = event.getTagsSafely()
        if (tags.isNotEmpty()) {
            Text(
                text = "Tags: ${tags.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Description
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = event.description,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Teams
        val teams = event.getTeamsSafely()
        Text(
            text = "Teams (${teams.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val teamObjects = event.getTeamObjectsSafely()
        if (teamObjects.isEmpty()) {
            Text(
                text = "No teams yet",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            teamObjects.forEach { team ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Members: ${team.members.size}/${event.maxTeamSize}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "Status: ${team.status}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Registration button
        if (!isUserRegistered) {
            Button(
                onClick = { /* TODO: Implement registration */ },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Register for Event")
            }
        } else {
            OutlinedButton(
                onClick = { /* TODO: Implement unregistration */ },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Unregister from Event")
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}