package views

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import api.MoodTrackerClient
import dto.EntryDto
import extension.EntrySortOrder
import extension.filterByMoodRange
import extension.filterBySearchQuery
import extension.displayMood
import extension.sortedByCreatedAt
import extension.toDisplayTimestamp
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
    var minMoodFilter by remember { mutableStateOf<Int?>(null) }
    var maxMoodFilter by remember { mutableStateOf<Int?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var minMoodInput by remember { mutableStateOf("") }
    var maxMoodInput by remember { mutableStateOf("") }
    var dialogSortAscending by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    suspend fun refreshEntries() {
        isLoading = true
        errorMessage = null
        try {
            entries = client.getEntries(userId)
        } catch (ex: Exception) {
            errorMessage = ex.message ?: "Unable to load entries."
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search entries") },
                    modifier = Modifier.weight(1f)
                )
                FilledTonalButton(
                    onClick = {
                        minMoodInput = minMoodFilter?.toString().orEmpty()
                        maxMoodInput = maxMoodFilter?.toString().orEmpty()
                        dialogSortAscending = isAscending
                        showFilterDialog = true
                    }
                ) {
                    Text("Sort & Filter")
                }
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            val sortOrder = if (isAscending) EntrySortOrder.ASC else EntrySortOrder.DESC
            val sortedEntries = remember(entries, sortOrder, minMoodFilter, maxMoodFilter, searchQuery) {
                entries
                    .filterByMoodRange(minMoodFilter, maxMoodFilter)
                    .filterBySearchQuery(searchQuery)
                    .sortedByCreatedAt(sortOrder)
            }

            if (!isLoading && sortedEntries.isEmpty()) {
                Text(
                    text = "No entries yet. Create your first entry to get started.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isLoading) {
                EntryListSkeleton(modifier = Modifier.fillMaxSize())
            } else {
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
                                                isLoading = true
                                                errorMessage = null
                                                try {
                                                    client.deleteEntry(entry.id)
                                                    entries = entries.filterNot { it.id == entry.id }
                                                } catch (ex: Exception) {
                                                    errorMessage =
                                                        ex.message ?: "Unable to delete entry."
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

        if (showFilterDialog) {
            AlertDialog(
                onDismissRequest = { showFilterDialog = false },
                title = { Text("Filter & sort") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = minMoodInput,
                            onValueChange = { minMoodInput = it },
                            label = { Text("Min mood rating") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = maxMoodInput,
                            onValueChange = { maxMoodInput = it },
                            label = { Text("Max mood rating") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Text(
                            text = "Sort order",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = dialogSortAscending,
                                onClick = { dialogSortAscending = true }
                            )
                            Text("Oldest first")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = !dialogSortAscending,
                                onClick = { dialogSortAscending = false }
                            )
                            Text("Newest first")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val minMood = minMoodInput.toIntOrNull()
                            val maxMood = maxMoodInput.toIntOrNull()
                            if (minMood != null && maxMood != null && minMood > maxMood) {
                                minMoodFilter = null
                                maxMoodFilter = null
                                minMoodInput = ""
                                maxMoodInput = ""
                            } else {
                                minMoodFilter = minMood
                                maxMoodFilter = maxMood
                            }
                            isAscending = dialogSortAscending
                            showFilterDialog = false
                            scope.launch {
                                listState.animateScrollToItem(0)
                            }
                        }
                    ) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showFilterDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun EntryListSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "entry-list-skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "entry-list-skeleton-alpha"
    )
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f * alpha)
    val cardColor = MaterialTheme.colorScheme.surfaceVariant

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(6) { _ ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor)
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
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(20.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(placeholderColor)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(14.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(placeholderColor)
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(12.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(placeholderColor)
                            )
                            Box(
                                modifier = Modifier
                                    .width(72.dp)
                                    .height(22.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(placeholderColor)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(placeholderColor)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(placeholderColor)
                        )
                    }
                }
            }
        }
    }
}
