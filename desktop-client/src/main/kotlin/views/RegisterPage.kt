package views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import api.MoodTrackerClient
import kotlinx.coroutines.launch
import model.RegisterInput
import model.RegisterModel
import model.RegisterResult

@Composable
fun RegisterPage(
    client: MoodTrackerClient,
    onNavigateBack: () -> Unit,
    onNavigateToEntries: (Long) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val registerModel = remember(client) { RegisterModel(client) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                isLoading = true
                statusMessage = null
                errorMessage = null
                scope.launch {
                    when (
                        val result = registerModel.register(
                            RegisterInput(
                                username = username,
                                email = email,
                                password = password
                            )
                        )
                    ) {
                        is RegisterResult.ValidationError -> {
                            errorMessage = "Bitte alle Felder ausfüllen."
                        }
                        is RegisterResult.Success -> {
                            statusMessage = "Registrierung erfolgreich. Willkommen, ${result.user.username}!"
                            onNavigateToEntries(result.loginResponse.userId)
                        }
                        is RegisterResult.Failure -> {
                            errorMessage = result.message ?: "Registrierung fehlgeschlagen."
                        }
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading
        ) {
            Text("Register")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }

        statusMessage?.let { message ->
            Text(message)
        }

        errorMessage?.let { message ->
            Text(message, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
        }
    }
}
