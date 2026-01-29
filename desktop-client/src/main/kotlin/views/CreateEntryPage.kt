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
import kotlinx.coroutines.launch
import model.CreateEntryInput
import model.CreateEntryModel
import model.CreateEntryPreparation
import model.CreateEntryResult

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
    val createEntryModel = remember(client) { CreateEntryModel(client) }

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
                val input = CreateEntryInput(
                    title = title,
                    content = content,
                    moodRatingInput = moodRatingInput
                )
                when (val prepared = createEntryModel.prepare(input)) {
                    is CreateEntryPreparation.ValidationError -> {
                        errorMessage = createEntryModel.validationMessage(prepared.validation)
                            ?: "Bitte Eingaben prüfen."
                        statusMessage = null
                        return@Button
                    }
                    is CreateEntryPreparation.Ready -> Unit
                }

                isLoading = true
                statusMessage = null
                errorMessage = null
                scope.launch {
                    when (val result = createEntryModel.createEntry(userId, input)) {
                        is CreateEntryResult.Success -> {
                            statusMessage = "Eintrag wurde erstellt."
                            title = ""
                            content = ""
                            moodRatingInput = ""
                            onNavigateBack()
                        }
                        is CreateEntryResult.ValidationError -> {
                            errorMessage = createEntryModel.validationMessage(result.validation)
                                ?: "Bitte Eingaben prüfen."
                        }
                        is CreateEntryResult.Failure -> {
                            errorMessage = result.message ?: "Eintrag konnte nicht erstellt werden."
                        }
                    }
                    isLoading = false
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
