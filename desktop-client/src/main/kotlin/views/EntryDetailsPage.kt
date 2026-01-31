package views

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import api.MoodTrackerClient
import dto.EntryDto
import extension.displayMood
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

            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (isLoading) {
                EntryDetailsSkeleton(modifier = Modifier.fillMaxWidth())
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
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

                            Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.End
                            ) {
                                Text(
                                    text = "Mood rating",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = entry.displayMood(unknownText = "—"),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
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

@Composable
private fun EntryDetailsSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "entry-details-skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "entry-details-skeleton-alpha"
    )
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f * alpha)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(24.dp)
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(14.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(placeholderColor)
                    )
                }

                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(84.dp)
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

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(16.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(placeholderColor)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(placeholderColor)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(16.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(placeholderColor)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(18.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(placeholderColor)
                )
            }
        }
    }
}
