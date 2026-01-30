package views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import api.MoodTrackerClient
import dto.EntryDto
import extension.toDisplayTimestamp
import extension.toEmoji

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

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Entry details",
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = "Review what you captured and keep track of the mood timeline.",
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
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            entryDetails?.let { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Created ${entry.createdAt.toDisplayTimestamp()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Updated ${entry.updatedAt?.toDisplayTimestamp() ?: "—"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = entry.content,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Mood rating",
                                style = MaterialTheme.typography.titleMedium
                            )
                            val moodText = entry.moodRating?.let { rating ->
                                "$rating ${rating.toEmoji()}"
                            } ?: "—"
                            Text(
                                text = moodText.trim(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Tags",
                                style = MaterialTheme.typography.titleMedium
                            )
                            val tagText = entry.tags.joinToString().ifBlank { "—" }
                            Text(
                                text = tagText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            if (entryDetails == null && !isLoading && errorMessage == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No entry data available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}