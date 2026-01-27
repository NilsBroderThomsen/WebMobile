package views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import api.MoodTrackerClient
import dto.CreateEntryRequest
import kotlinx.coroutines.launch

@Composable
fun CreateEntryPage(
    client: MoodTrackerClient,
    userId: Long,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var moodRatingInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }

        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = moodRatingInput,
            onValueChange = { moodRatingInput = it },
            label = { Text("Mood Rating (1-10, optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val trimmedTitle = title.trim()
                val trimmedContent = content.trim()
                val trimmedMood = moodRatingInput.trim()

                if (trimmedTitle.isBlank() || trimmedContent.isBlank()) {
                    errorMessage = "Bitte Title und Content ausfüllen."
                    statusMessage = null
                    return@Button
                }

                val moodRating = if (trimmedMood.isBlank()) {
                    null
                } else {
                    trimmedMood.toIntOrNull()
                }

                if (trimmedMood.isNotBlank() && moodRating == null) {
                    errorMessage = "Mood Rating muss eine Zahl zwischen 1 und 10 sein."
                    statusMessage = null
                    return@Button
                }

                if (moodRating != null && moodRating !in 1..10) {
                    errorMessage = "Mood Rating muss eine Zahl zwischen 1 und 10 sein."
                    statusMessage = null
                    return@Button
                }

                isLoading = true
                statusMessage = null
                errorMessage = null
                scope.launch {
                    try {
                        client.createEntry(
                            userId = userId,
                            request = CreateEntryRequest(
                                title = trimmedTitle,
                                content = trimmedContent,
                                moodRating = moodRating
                            )
                        )
                        statusMessage = "Eintrag wurde erstellt."
                        title = ""
                        content = ""
                        moodRatingInput = ""
                        onNavigateBack()
                    } catch (ex: Exception) {
                        errorMessage = ex.message ?: "Eintrag konnte nicht erstellt werden."
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text("Eintrag erstellen")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }

        statusMessage?.let { message ->
            Text(message)
        }

        errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
        }
    }
}
