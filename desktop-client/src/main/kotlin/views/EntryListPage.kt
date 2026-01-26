package views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import config.AppConfig
import dto.EntryDto

@Composable
fun EntryListPage(
    userId: Long,
    onNavigateBack: () -> Unit,
    onCreateEntry: () -> Unit
) {
    var entries by remember { mutableStateOf<List<EntryDto>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val client = MoodTrackerClient(AppConfig.BASE_URL)
        try {
            entries = client.getEntries(userId)
        } catch (ex: Exception) {
            errorMessage = ex.message ?: "Einträge konnten nicht geladen werden."
        } finally {
            isLoading = false
            client.close()
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
            Text("Entry ID: ${entry.id}, Title: ${entry.title}, Content: ${entry.content}, Mood: ${entry.moodRating}")
        }
    }
}
