package views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import api.MoodTrackerClient
import dto.EntryDto
import kotlinx.coroutines.launch
import viewmodel.UpdateEntryInput
import viewmodel.UpdateEntryViewModel
import viewmodel.UpdateEntryResult
import viewmodel.UpdateEntryValidation

@Composable
fun UpdateEntryPage(
    client: MoodTrackerClient,
    entryDto: EntryDto,
    onNavigateBack: () -> Unit
) {
    var title by remember(entryDto.id) { mutableStateOf(entryDto.title) }
    var content by remember(entryDto.id) { mutableStateOf(entryDto.content) }
    var moodRatingInput by remember(entryDto.id) { mutableStateOf(entryDto.moodRating?.toString() ?: "") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var contentError by remember { mutableStateOf<String?>(null) }
    var moodError by remember { mutableStateOf<String?>(null) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val updateEntryViewModel = remember(client) { UpdateEntryViewModel(client) }

    fun clearErrors() {
        titleError = null
        contentError = null
        moodError = null
        generalError = null
    }

    fun applyValidation(v: UpdateEntryValidation) {
        titleError = if (v.missingTitle) "Titel erforderlich" else null
        contentError = if (v.missingContent) "Content erforderlich" else null

        moodError = when {
            v.invalidMoodFormat -> "Mood Rating muss eine Zahl sein"
            v.moodOutOfRange -> "Mood Rating muss zwischen 1 und 10 sein"
            else -> null
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
                    text = "Update entry",
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = "Refine your thoughts and adjust today’s mood.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Entry details",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                titleError = null
                                generalError = null
                            },
                            label = { Text("Title") },
                            isError = titleError != null,
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = {
                                if (titleError != null) {
                                    Text(titleError ?: "")
                                } else {
                                    Text("Keep it short and descriptive.")
                                }
                            }
                        )

                        OutlinedTextField(
                            value = content,
                            onValueChange = {
                                content = it
                                contentError = null
                                generalError = null
                            },
                            label = { Text("Notes") },
                            isError = contentError != null,
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = {
                                if (contentError != null) {
                                    Text(contentError ?: "")
                                } else {
                                    Text("What stood out today?")
                                }
                            }
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Mood rating",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = moodRatingInput,
                            onValueChange = {
                                moodRatingInput = it
                                moodError = null
                                generalError = null
                            },
                            label = { Text("Rating (1-10, optional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = moodError != null,
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = {
                                if (moodError != null) {
                                    Text(moodError ?: "")
                                } else {
                                    Text("Leave blank if you prefer to skip.")
                                }
                            }
                        )
                    }

                    statusMessage?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    generalError?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = onNavigateBack, enabled = !isLoading) {
                            Text("Back")
                        }
                        Button(
                            enabled = !isLoading,
                            onClick = {
                                if (isLoading) return@Button

                                clearErrors()
                                statusMessage = null

                                val input = UpdateEntryInput(
                                    title = title,
                                    content = content,
                                    moodRatingInput = moodRatingInput
                                )

                                isLoading = true
                                scope.launch {
                                    try {
                                        when (val result = updateEntryViewModel.updateEntry(entryDto.id, input)) {
                                            is UpdateEntryResult.Success -> {
                                                statusMessage = "Entry updated successfully."
                                                onNavigateBack()
                                            }

                                            is UpdateEntryResult.ValidationError -> {
                                                applyValidation(result.validation)
                                                generalError = "Please check your input."
                                            }

                                            is UpdateEntryResult.Failure -> {
                                                generalError =
                                                    result.message ?: "Unable to update entry."
                                            }
                                        }
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        ) {
                            Text("Save changes")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (isLoading) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
