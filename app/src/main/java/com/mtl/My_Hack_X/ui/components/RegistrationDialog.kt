package com.mtl.My_Hack_X.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mtl.My_Hack_X.data.models.HackathonEvent
import com.mtl.My_Hack_X.data.models.RegistrationData
import com.mtl.My_Hack_X.data.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationDialog(
    event: HackathonEvent,
    currentUser: User,
    onDismiss: () -> Unit,
    onConfirm: (RegistrationData) -> Unit
) {
    var teamRegistration by remember { mutableStateOf(event.minTeamSize > 1) }
    var teamName by remember { mutableStateOf("Team ${currentUser.displayName}") }
    var memberEmails by remember { mutableStateOf(mutableListOf<String>()) }
    var newEmail by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register for ${event.name}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Registration type selector (if applicable)
                if (event.minTeamSize == 1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Registration Type:", modifier = Modifier.weight(1f))
                        Switch(
                            checked = teamRegistration,
                            onCheckedChange = { teamRegistration = it }
                        )
                        Text(
                            text = if (teamRegistration) "Team" else "Individual",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                
                // Team name input (if team registration)
                if (teamRegistration) {
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Team Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Team Members")
                    Text(
                        text = "You are automatically included as team leader",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    // Team size info
                    Text(
                        text = "Min: ${event.minTeamSize}, Max: ${event.maxTeamSize}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    // Current team members
                    memberEmails.forEachIndexed { index, email ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(email, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                memberEmails.removeAt(index)
                                memberEmails = ArrayList(memberEmails)
                            }) {
                                Icon(Icons.Default.Delete, "Remove Member")
                            }
                        }
                    }
                    
                    // Add new member
                    if (memberEmails.size < event.maxTeamSize - 1) { // -1 because the user is already included
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newEmail,
                                onValueChange = { newEmail = it },
                                label = { Text("Member Email") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                            )
                            IconButton(
                                onClick = {
                                    if (newEmail.isNotEmpty() && !memberEmails.contains(newEmail)) {
                                        memberEmails.add(newEmail)
                                        memberEmails = ArrayList(memberEmails)
                                        newEmail = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, "Add Member")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        if (teamRegistration) {
                            RegistrationData.TeamRegistrationData(
                                eventId = event.id,
                                teamName = teamName,
                                memberEmails = memberEmails + currentUser.email,
                                leaderId = currentUser.uid
                            )
                        } else {
                            RegistrationData.IndividualRegistrationData(
                                eventId = event.id,
                                userId = currentUser.uid
                            )
                        }
                    )
                }
            ) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 