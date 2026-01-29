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
import model.LoginInput
import model.LoginModel
import model.LoginResult

@Composable
fun LoginPage(
    client: MoodTrackerClient,
    onNavigateBack: () -> Unit,
    onNavigateToEntries: (Long) -> Unit
) {
    val loginModel = remember(client) { LoginModel(client) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Button (
            onClick = {
                isLoading = true
                statusMessage = null
                errorMessage = null

                scope.launch {
                    when (
                        val result = loginModel.login(
                            LoginInput(
                                username = username,
                                password = password
                            )
                        )
                    ) {
                        is LoginResult.ValidationError -> {
                            errorMessage = "Bitte alle Felder ausfüllen."
                        }
                        is LoginResult.Success -> {
                            statusMessage = "Login erfolgreich. Willkommen, $username!"
                            onNavigateToEntries(result.loginResponse.userId)
                        }
                        is LoginResult.Failure -> {
                            errorMessage = result.message ?: "Login fehlgeschlagen."
                        }
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading
        ) {
            Text("Login")
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
