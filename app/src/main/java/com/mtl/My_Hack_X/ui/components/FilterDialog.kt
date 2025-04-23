package com.mtl.My_Hack_X.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mtl.My_Hack_X.data.models.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentFilter: EventFilter = EventFilter(),
    onApply: (EventFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var status by remember { mutableStateOf(currentFilter.status) }
    var location by remember { mutableStateOf(currentFilter.location ?: "") }
    var maxTeamSize by remember { mutableStateOf(currentFilter.maxTeamSize?.toString() ?: "") }
    var tags by remember { mutableStateOf(currentFilter.tags.joinToString(",")) }
    
    var startDate by remember { mutableStateOf(currentFilter.startDate?.let { formatDate(it) } ?: "") }
    var endDate by remember { mutableStateOf(currentFilter.endDate?.let { formatDate(it) } ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Events") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                    value = maxTeamSize,
                    onValueChange = { maxTeamSize = it.filter { c -> c.isDigit() } },
                    label = { Text("Max Team Size") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

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
                    val newFilter = EventFilter(
                        status = status,
                        location = location.takeIf { it.isNotBlank() },
                        maxTeamSize = maxTeamSize.toIntOrNull(),
                        tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        startDate = try { dateFormat.parse(startDate) } catch (e: Exception) { null },
                        endDate = try { dateFormat.parse(endDate) } catch (e: Exception) { null }
                    )
                    onApply(newFilter)
                    onDismiss()
                }
            ) {
                Text("Apply")
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