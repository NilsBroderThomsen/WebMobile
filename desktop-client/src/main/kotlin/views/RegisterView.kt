package views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
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

@Composable
fun RegisterView(
    onNavigateBack: () -> Unit,
    onNavigateToEntries: () -> Unit
) {
    val baseUrl = "http://localhost:8080"
    val coroutineScope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Column {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isSubmitting) return@Button
                coroutineScope.launch {
                    isSubmitting = true
                    statusMessage = null
                    val client = MoodTrackerClient(baseUrl)
                    val result = runCatching {
                        client.postCreateUser(username, email, password)
                    }
                    client.close()
                    isSubmitting = false
                    result
                        .onSuccess {
                            statusMessage = "Registration successful."
                            onNavigateToEntries()
                        }
                        .onFailure { error ->
                            statusMessage = "Registration failed: ${error.message ?: "Unknown error"}"
                        }
                }
            }
        ) {
            Text(if (isSubmitting) "Registering..." else "Register")
        }

        statusMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(message)
        }
    }
}
