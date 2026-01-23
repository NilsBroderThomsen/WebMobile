import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import api.MoodTrackerClient
import dto.CreateEntryRequest
import dto.EntryDto
import kotlinx.coroutines.launch

private const val USER_ID = 1L
private const val BASE_URL = "http://localhost:8080"

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Moodtracker Desktop",
    ) {
        MaterialTheme {
            MoodTrackerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodTrackerApp() {
    val client = remember { MoodTrackerClient(BASE_URL) }
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<EntryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var moodRating by remember { mutableStateOf("") }

    fun refreshEntries() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                entries = client.getEntries(USER_ID)
            } catch (ex: Exception) {
                errorMessage = "Einträge konnten nicht geladen werden."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshEntries()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Moodtracker") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EntryForm(
                title = title,
                content = content,
                moodRating = moodRating,
                isSubmitting = isLoading,
                onTitleChange = { title = it },
                onContentChange = { content = it },
                onMoodRatingChange = { moodRating = it },
                onSubmit = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val rating = moodRating.trim().toIntOrNull()
                            client.createEntry(
                                USER_ID,
                                CreateEntryRequest(
                                    title = title.trim(),
                                    content = content.trim(),
                                    moodRating = rating
                                )
                            )
                            title = ""
                            content = ""
                            moodRating = ""
                            refreshEntries()
                        } catch (ex: Exception) {
                            errorMessage = "Eintrag konnte nicht erstellt werden."
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                EntryList(entries = entries)
            }
        }
    }
}

@Composable
private fun EntryForm(
    title: String,
    content: String,
    moodRating: String,
    isSubmitting: Boolean,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onMoodRatingChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Neuen Eintrag erstellen", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Titel") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                label = { Text("Inhalt") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = moodRating,
                onValueChange = onMoodRatingChange,
                label = { Text("Mood Rating (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onSubmit,
                enabled = title.isNotBlank() && content.isNotBlank() && !isSubmitting,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Eintrag speichern")
            }
        }
    }
}

@Composable
private fun EntryList(entries: List<EntryDto>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Alle Einträge", style = MaterialTheme.typography.titleMedium)
        if (entries.isEmpty()) {
            Text("Noch keine Einträge vorhanden.")
        } else {
            entries.forEach { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(entry.title, style = MaterialTheme.typography.titleSmall)
                        Text(entry.content, style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Mood: ${entry.moodRating ?: "-"}")
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Erstellt: ${entry.createdAt}")
                        }
                    }
                }
            }
        }
    }
}
