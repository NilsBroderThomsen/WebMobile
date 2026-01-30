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
import extension.EntrySortOrder
import extension.displayMood
import extension.sortedByCreatedAt
import extension.toDisplayTimestamp
import kotlinx.coroutines.launch
import state.LoadState
import state.fetchLoadState

@Composable
fun EntryListPage(
    client: MoodTrackerClient,
    userId: Long,
    onCreateEntry: () -> Unit,
    onUpdateEntry: (EntryDto) -> Unit,
    onEntrySelected: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    var entriesState by remember { mutableStateOf<LoadState<List<EntryDto>>>(LoadState.Loading) }
    var actionErrorMessage by remember { mutableStateOf<String?>(null) }
    var isMutating by remember { mutableStateOf(false) }
    var isAscending by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    suspend fun refreshEntries() {
        entriesState = LoadState.Loading
        actionErrorMessage = null
        entriesState = fetchLoadState("Einträge konnten nicht geladen werden.") {
            client.getEntries(userId)
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

            val isLoading = entriesState is LoadState.Loading || isMutating
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            val errorMessage = (entriesState as? LoadState.Error)?.message ?: actionErrorMessage

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            val sortOrder = if (isAscending) EntrySortOrder.ASC else EntrySortOrder.DESC
            val entries = (entriesState as? LoadState.Success)?.data.orEmpty()
            val sortedEntries = remember(entries, sortOrder) {
                entries.sortedByCreatedAt(sortOrder)
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
                                        text = entry.displayMood(unknownText = "—"),
                                        style = MaterialTheme.typography.titleLarge
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
                                            isMutating = true
                                            actionErrorMessage = null
                                            try {
                                                client.deleteEntry(entry.id)
                                                entriesState = when (val state = entriesState) {
                                                    is LoadState.Success -> {
                                                        state.copy(data = state.data.filterNot { it.id == entry.id })
                                                    }
                                                    else -> state
                                                }
                                            } catch (ex: Exception) {
                                                actionErrorMessage =
                                                    ex.message ?: "Eintrag konnte nicht gelöscht werden."
                                            } finally {
                                                isMutating = false
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
