package views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import api.MoodTrackerClient
import dto.EntryDto

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

    LaunchedEffect(Unit) {
        try {
            entries = client.getEntries(userId)
        } catch (ex: Exception) {
            errorMessage = ex.message ?: "Einträge konnten nicht geladen werden."
        } finally {
            isLoading = false
        }
    }

    Column {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }

        Button(onClick = onCreateEntry) {
            Text("Create New Entry")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }

        errorMessage?.let { message ->
            Text(message)
        }

        entries.forEach { entry ->
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.clickable { onEntrySelected(entry.id) }
            ) {
                Text("Title: ${entry.title}")
                Text("Mood: ${entry.moodRating ?: "—"}")
                Text("Created: ${entry.createdAt}")
            }
            Button(onClick = { onUpdateEntry(entry) }) { Text("Update") }
        }
    }
}
