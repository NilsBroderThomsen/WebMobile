package views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import api.MoodTrackerClient
import kotlinx.coroutines.launch
import model.CreateEntryInput
import model.CreateEntryModel
import model.CreateEntryResult
import model.CreateEntryValidation

@Composable
fun CreateEntryPage(
    client: MoodTrackerClient,
    userId: Long,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var moodRatingInput by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var contentError by remember { mutableStateOf<String?>(null) }
    var moodError by remember { mutableStateOf<String?>(null) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val createEntryModel = remember(client) { CreateEntryModel(client) }

    fun clearErrors() {
        titleError = null
        contentError = null
        moodError = null
        generalError = null
    }

    fun applyValidation(v: CreateEntryValidation) {
        titleError = if (v.missingTitle) "Titel erforderlich" else null
        contentError = if (v.missingContent) "Content erforderlich" else null

        moodError = when {
            v.invalidMoodFormat -> "Mood Rating muss eine Zahl sein"
            v.moodOutOfRange -> "Mood Rating muss zwischen 1 und 10 sein"
            else -> null
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onNavigateBack, enabled = !isLoading) {
            Text("Back")
        }

        TextField(
            value = title,
            onValueChange = {
                title = it
                titleError = null
                generalError = null
            },
            label = { Text("Title") },
            isError = titleError != null,
            modifier = Modifier.fillMaxWidth()
        )
        titleError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        TextField(
            value = content,
            onValueChange = {
                content = it
                contentError = null
                generalError = null
            },
            label = { Text("Content") },
            isError = contentError != null,
            modifier = Modifier.fillMaxWidth()
        )
        contentError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        TextField(
            value = moodRatingInput,
            onValueChange = {
                moodRatingInput = it
                moodError = null
                generalError = null
            },
            label = { Text("Mood Rating (1-10, optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = moodError != null,
            modifier = Modifier.fillMaxWidth()
        )
        moodError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            enabled = !isLoading,
            onClick = {
                if (isLoading) return@Button

                clearErrors()
                statusMessage = null

                val input = CreateEntryInput(
                    title = title,
                    content = content,
                    moodRatingInput = moodRatingInput
                )

                isLoading = true
                scope.launch {
                    try {
                        when (val result = createEntryModel.createEntry(userId, input)) {
                            is CreateEntryResult.Success -> {
                                statusMessage = "Eintrag wurde erstellt."
                                title = ""
                                content = ""
                                moodRatingInput = ""
                                onNavigateBack()
                            }

                            is CreateEntryResult.ValidationError -> {
                                applyValidation(result.validation)
                                generalError = "Bitte Eingaben prüfen."
                            }

                            is CreateEntryResult.Failure -> {
                                generalError = result.message ?: "Eintrag konnte nicht erstellt werden."
                            }
                        }
                    } finally {
                        isLoading = false
                    }
                }
            }
        ) {
            Text("Eintrag erstellen")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }

        statusMessage?.let { Text(it) }
        generalError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
