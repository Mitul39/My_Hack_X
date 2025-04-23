package com.mtl.My_Hack_X.ui.screens.event

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mtl.My_Hack_X.data.models.RegistrationType
import com.mtl.My_Hack_X.data.models.Team
import com.mtl.My_Hack_X.ui.components.ErrorMessage
import com.mtl.My_Hack_X.ui.components.LoadingSpinner
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    navController: NavController,
    viewModel: EventDetailsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var showTeamDialog by remember { mutableStateOf(false) }
    var teamName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state) {
                is EventDetailsState.Loading -> LoadingSpinner()
                is EventDetailsState.Error -> ErrorMessage(
                    message = (state as EventDetailsState.Error).message,
                    onRetry = { viewModel.loadEventDetails() }
                )
                is EventDetailsState.Success -> {
                    val successState = state as EventDetailsState.Success
                    EventContent(
                        state = successState,
                        onRegister = {
                            if (successState.event.registrationType == RegistrationType.TEAM) {
                                showTeamDialog = true
                            } else {
                                viewModel.registerForEvent()
                            }
                        },
                        onLeave = { viewModel.leaveEvent() },
                        onJoinTeam = { teamId -> viewModel.joinTeam(teamId) }
                    )
                }
            }
        }
    }

    if (showTeamDialog) {
        AlertDialog(
            onDismissRequest = { showTeamDialog = false },
            title = { Text("Create Team") },
            text = {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Team Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.registerForEvent(teamName)
                        showTeamDialog = false
                        teamName = ""
                    },
                    enabled = teamName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTeamDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EventContent(
    state: EventDetailsState.Success,
    onRegister: () -> Unit,
    onLeave: () -> Unit,
    onJoinTeam: (String) -> Unit
) {
    val event = state.event

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        state.eventImage?.let { imageUrl ->
            item {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Event Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        item {
            Text(
                text = event.name,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        item {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Event Details",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Date: ${formatDate(event.startDate)} - ${formatDate(event.endDate)}")
                    Text("Location: ${event.location}")
                    Text("Registration: ${event.registrationType.name}")
                    Text("Team Size: ${event.minTeamSize}-${event.maxTeamSize} members")
                }
            }
        }

        if (event.tags.isNotEmpty()) {
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
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
            }
        }

        if (event.prizes.isNotEmpty()) {
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Prizes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        event.prizes.forEach { prize ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${prize.title}: ${prize.description}")
                                Text("$${prize.value}")
                            }
                        }
                    }
                }
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Teams",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (state.teams.isEmpty()) {
                        Text(
                            text = "No teams registered yet",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        state.teams.forEach { team ->
                            TeamItem(
                                team = team,
                                currentUserTeamId = state.currentUserTeamId,
                                canJoin = state.canJoinTeam,
                                onJoinTeam = { onJoinTeam(team.id) }
                            )
                            Divider()
                        }
                    }
                }
            }
        }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                when {
                    state.isRegistered -> {
                        Button(
                            onClick = onLeave,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Leave Event")
                        }
                    }
                    state.canRegister -> {
                        Button(onClick = onRegister) {
                            Text("Register for Event")
                        }
                    }
                    else -> {
                        Text(
                            text = "Registration is currently closed.",
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamItem(
    team: Team,
    currentUserTeamId: String?,
    canJoin: Boolean,
    onJoinTeam: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = team.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${team.members.size} member(s)",
                style = MaterialTheme.typography.bodySmall
            )
            if (team.id == currentUserTeamId) {
                Text(
                    text = "Your Team",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (team.id != currentUserTeamId && canJoin) {
            Button(onClick = onJoinTeam) {
                Text("Join")
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
} 