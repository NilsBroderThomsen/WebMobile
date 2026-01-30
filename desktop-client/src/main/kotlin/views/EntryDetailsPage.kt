package views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import api.MoodTrackerClient
import dto.EntryDto
import extension.toDisplayTimestamp

@Composable
fun EntryDetailsPage(
    client: MoodTrackerClient,
    entryId: Long,
    onNavigateBack: () -> Unit
) {
    var entryDetails by remember { mutableStateOf<EntryDto?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(entryId) {
        try {
            entryDetails = client.getEntryDetails(entryId)
        } catch (ex: Exception) {
            errorMessage = ex.message ?: "Eintrag konnte nicht geladen werden."
        } finally {
            isLoading = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }

        errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
        }

        entryDetails?.let { entry ->
            Text("Title: ${entry.title}")
            Text("Content: ${entry.content}")
            Text("Mood: ${entry.moodRating ?: "—"}")
            Text("Created: ${entry.createdAt.toDisplayTimestamp()}")
            Text("Updated: ${entry.updatedAt?.toDisplayTimestamp() ?: "—"}")
            Text("Tags: ${entry.tags.joinToString().ifBlank { "—" }}")
        }
    }
}
