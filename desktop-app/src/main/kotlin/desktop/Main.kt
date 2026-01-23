package desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import api.MoodTrackerClient
import dto.EntryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val FixedUserId = 1L
private const val BaseUrl = "http://localhost:8080"

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "MoodTracker - Einträge") {
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                EntryListScreen()
            }
        }
    }
}

@Composable
private fun EntryListScreen() {
    var entries by remember { mutableStateOf<List<EntryDto>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val client = MoodTrackerClient(BaseUrl)
        runCatching {
            withContext(Dispatchers.IO) { client.getEntries(FixedUserId) }
        }.onSuccess {
            entries = it
        }.onFailure { throwable ->
            errorMessage = throwable.message ?: "Unbekannter Fehler beim Laden"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Alle Einträge (User $FixedUserId)",
            style = MaterialTheme.typography.headlineSmall
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries, key = { it.id }) { entry ->
                EntryCard(entry)
            }
        }
    }
}

@Composable
private fun EntryCard(entry: EntryDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = entry.title, style = MaterialTheme.typography.titleMedium)
            Text(text = entry.content, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = buildString {
                    append("Mood: ")
                    append(entry.moodRating?.toString() ?: "-")
                    append(" · Erstellt: ")
                    append(entry.createdAt)
                },
                style = MaterialTheme.typography.bodySmall
            )
            if (entry.tags.isNotEmpty()) {
                Text(
                    text = "Tags: ${entry.tags.joinToString()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
