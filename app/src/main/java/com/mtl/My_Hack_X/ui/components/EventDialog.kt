package com.mtl.My_Hack_X.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mtl.My_Hack_X.data.models.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDialog(
    event: HackathonEvent? = null,
    onSave: (HackathonEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(event?.name ?: "") }
    var description by remember { mutableStateOf(event?.description ?: "") }
    var location by remember { mutableStateOf(event?.location ?: "") }
    var maxTeamSize by remember { mutableStateOf(event?.maxTeamSize?.toString() ?: "4") }
    var minTeamSize by remember { mutableStateOf(event?.minTeamSize?.toString() ?: "1") }
    var registrationType by remember { mutableStateOf(event?.registrationType ?: RegistrationType.TEAM) }
    var status by remember { mutableStateOf(event?.status ?: EventStatus.UPCOMING) }
    
    var startDate by remember { mutableStateOf(formatDate(event?.startDate ?: Date())) }
    var endDate by remember { mutableStateOf(formatDate(event?.endDate ?: Date())) }
    var registrationDeadline by remember { mutableStateOf(formatDate(event?.registrationDeadline ?: Date())) }
    
    var tags by remember { mutableStateOf(event?.tags?.joinToString(",") ?: "") }
    var rules by remember { mutableStateOf(event?.rules?.joinToString("\n") ?: "") }
    var prizes by remember { mutableStateOf("") } // We'll handle prizes separately if needed

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (event == null) "Create Event" else "Edit Event") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Event Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date (MM/dd/yyyy)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date (MM/dd/yyyy)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = registrationDeadline,
                    onValueChange = { registrationDeadline = it },
                    label = { Text("Registration Deadline (MM/dd/yyyy)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minTeamSize,
                        onValueChange = { minTeamSize = it.filter { c -> c.isDigit() } },
                        label = { Text("Min Team Size") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxTeamSize,
                        onValueChange = { maxTeamSize = it.filter { c -> c.isDigit() } },
                        label = { Text("Max Team Size") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rules,
                    onValueChange = { rules = it },
                    label = { Text("Rules (one per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Column {
                    Text("Registration Type", style = MaterialTheme.typography.titleSmall)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RegistrationType.values().forEach { type ->
                            FilterChip(
                                selected = registrationType == type,
                                onClick = { registrationType = type },
                                label = { Text(type.name) }
                            )
                        }
                    }
                }

                Column {
                    Text("Status", style = MaterialTheme.typography.titleSmall)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EventStatus.values().forEach { s ->
                            FilterChip(
                                selected = status == s,
                                onClick = { status = s },
                                label = { Text(s.name) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    val newEvent = HackathonEvent(
                        id = event?.id ?: "",
                        name = name,
                        description = description,
                        location = location,
                        startDate = try { dateFormat.parse(startDate) ?: Date() } catch (e: Exception) { Date() },
                        endDate = try { dateFormat.parse(endDate) ?: Date() } catch (e: Exception) { Date() },
                        registrationDeadline = try { dateFormat.parse(registrationDeadline) ?: Date() } catch (e: Exception) { Date() },
                        minTeamSize = minTeamSize.toIntOrNull() ?: 1,
                        maxTeamSize = maxTeamSize.toIntOrNull() ?: 4,
                        organizerEmail = event?.organizerEmail ?: "",
                        status = status,
                        teams = event?.teams ?: emptyList(),
                        tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        rules = rules.split("\n").map { it.trim() }.filter { it.isNotEmpty() },
                        prizes = event?.prizes ?: emptyList(),
                        registrationType = registrationType
                    )
                    onSave(newEvent)
                    onDismiss()
                },
                enabled = name.isNotBlank() && description.isNotBlank() && location.isNotBlank() &&
                        startDate.isNotBlank() && endDate.isNotBlank() && registrationDeadline.isNotBlank() &&
                        minTeamSize.isNotBlank() && maxTeamSize.isNotBlank()
            ) {
                Text(if (event == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return sdf.format(date)
} 