package views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import api.MoodTrackerClient
import dto.EntryDto
import extension.toDisplayTimestamp
import extension.toEmoji
import kotlinx.coroutines.launch

@Composable
fun EntryListPage(
    client: MoodTrackerClient,
    userId: Long,
    onCreateEntry: () -> Unit,
    onUpdateEntry: (EntryDto) -> Unit,
    onEntrySelected: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    var entries by remember { mutableStateOf<List<EntryDto>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isAscending by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    suspend fun refreshEntries() {
        isLoading = true
        errorMessage = null
        try {
            entries = client.getEntries(userId)
        } catch (ex: Exception) {
            errorMessage = ex.message ?: "Einträge konnten nicht geladen werden."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(userId) {
        refreshEntries()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Your entries",
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = "Review your mood history and keep everything up to date.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onNavigateBack) {
                    Text("Back")
                }
                Button(onClick = onCreateEntry) {
                    Text("Create new entry")
                }
                Spacer(modifier = Modifier.weight(1f))
                FilledTonalButton(
                    onClick = {
                        isAscending = !isAscending
                        scope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                ) {
                    Text(if (isAscending) "Oldest first" else "Newest first")
                }
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            val sortedEntries = remember(entries, isAscending) {
                if (isAscending) {
                    entries.sortedBy { it.createdAt }
                } else {
                    entries.sortedByDescending { it.createdAt }
                }
            }

            if (!isLoading && sortedEntries.isEmpty()) {
                Text(
                    text = "No entries yet. Create your first entry to get started.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sortedEntries, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEntrySelected(entry.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = entry.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Created ${entry.createdAt.toDisplayTimestamp()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Mood",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${entry.moodRating ?: "—"} ${entry.moodRating?.toEmoji() ?: ""}".trim(),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { onUpdateEntry(entry) },
                                    enabled = !isLoading
                                ) {
                                    Text("Update")
                                }
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            isLoading = true
                                            errorMessage = null
                                            try {
                                                client.deleteEntry(entry.id)
                                                entries = entries.filterNot { it.id == entry.id }
                                            } catch (ex: Exception) {
                                                errorMessage =
                                                    ex.message ?: "Eintrag konnte nicht gelöscht werden."
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    },
                                    enabled = !isLoading
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
