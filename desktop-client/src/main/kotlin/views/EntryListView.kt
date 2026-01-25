package views

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import api.MoodTrackerClient
import dto.EntryDto

@Composable
fun EntryListView(
    userId: Long,
    onNavigateBack: () -> Unit,
    onCreateEntry: () -> Unit
) {
    val baseUrl = "http://localhost:8080"
    var entries by remember { mutableStateOf<List<EntryDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        val client = MoodTrackerClient(baseUrl)
        entries = client.getEntries(userId)
    }

    Column {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }

        Button(onClick = onCreateEntry) {
            Text("Create New Entry")
        }

        entries.forEach { entry ->
            Text("Entry ID: ${entry.id}, Title: ${entry.title}, Content: ${entry.content}, Mood: ${entry.moodRating}")
        }
    }
}
