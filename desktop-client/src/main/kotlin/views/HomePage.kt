package views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import api.MoodTrackerClient
import kotlinx.coroutines.launch
import model.LoginInput
import model.LoginModel
import model.LoginResult
import model.LoginValidation
import model.RegisterInput
import model.RegisterModel
import model.RegisterResult
import model.RegisterValidation

@Composable
fun HomePage(
    client: MoodTrackerClient,
    onAuthenticated: (Long) -> Unit,
    onNavigateToEntries: () -> Unit,
    showLogout: Boolean,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginUsernameError by remember { mutableStateOf<String?>(null) }
    var loginPasswordError by remember { mutableStateOf<String?>(null) }
    var loginStatusMessage by remember { mutableStateOf<String?>(null) }
    var isLoginLoading by remember { mutableStateOf(false) }

    var registerUsername by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerUsernameError by remember { mutableStateOf<String?>(null) }
    var registerEmailError by remember { mutableStateOf<String?>(null) }
    var registerPasswordError by remember { mutableStateOf<String?>(null) }
    var registerStatusMessage by remember { mutableStateOf<String?>(null) }
    var isRegisterLoading by remember { mutableStateOf(false) }

    val loginModel = remember(client) { LoginModel(client) }
    val registerModel = remember(client) { RegisterModel(client) }

    fun clearLoginErrors() {
        loginUsernameError = null
        loginPasswordError = null
    }

    fun clearRegisterErrors() {
        registerUsernameError = null
        registerEmailError = null
        registerPasswordError = null
    }

    fun applyLoginValidation(validation: LoginValidation) {
        loginUsernameError = when {
            validation.missingUsername -> "Username erforderlich"
            else -> null
        }
        loginPasswordError = when {
            validation.missingPassword -> "Passwort erforderlich"
            else -> null
        }
    }

    fun applyRegisterValidation(validation: RegisterValidation) {
        registerUsernameError = when {
            validation.missingUsername -> "Username erforderlich"
            validation.invalidUsername -> "Username zu kurz oder ungültig"
            else -> null
        }

        registerEmailError = when {
            validation.missingEmail -> "E-Mail erforderlich"
            validation.invalidEmail -> "Ungültige E-Mail-Adresse"
            else -> null
        }

        registerPasswordError = when {
            validation.missingPassword -> "Passwort erforderlich"
            validation.invalidPassword -> "Passwort muss mindestens 8 Zeichen haben"
            else -> null
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Mood Tracker",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Track your mood, reflect daily, and build healthier routines with a calm, focused workspace.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (showLogout) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Welcome back!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Jump right into your journal or review recent highlights.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onNavigateToEntries) {
                            Text("My Entries")
                        }
                        OutlinedButton(onClick = onLogout) {
                            Text("Logout")
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Sign in",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Access your entries and continue your streak.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = loginUsername,
                                onValueChange = {
                                    loginUsername = it
                                    loginUsernameError = null
                                },
                                label = { Text("Username") },
                                isError = loginUsernameError != null,
                                modifier = Modifier.fillMaxWidth()
                            )
                            loginUsernameError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }

                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = {
                                    loginPassword = it
                                    loginPasswordError = null
                                },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                isError = loginPasswordError != null,
                                modifier = Modifier.fillMaxWidth()
                            )
                            loginPasswordError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        ElevatedButton(
                            enabled = !isLoginLoading,
                            onClick = {
                                if (isLoginLoading) return@ElevatedButton

                                clearLoginErrors()
                                loginStatusMessage = null
                                isLoginLoading = true

                                scope.launch {
                                    try {
                                        when (
                                            val result = loginModel.login(
                                                LoginInput(
                                                    username = loginUsername,
                                                    password = loginPassword
                                                )
                                            )
                                        ) {
                                            is LoginResult.ValidationError -> {
                                                applyLoginValidation(result.validation)
                                            }

                                            is LoginResult.Success -> {
                                                loginStatusMessage =
                                                    "Login erfolgreich. Willkommen, ${loginUsername.trim()}!"
                                                onAuthenticated(result.loginResponse.userId)
                                            }

                                            is LoginResult.Failure -> {
                                                loginStatusMessage =
                                                    result.message ?: "Login fehlgeschlagen."
                                            }
                                        }
                                    } finally {
                                        isLoginLoading = false
                                    }
                                }
                            }
                        ) {
                            Text("Login")
                        }

                        if (isLoginLoading) {
                            CircularProgressIndicator()
                        }

                        loginStatusMessage?.let { msg ->
                            Text(
                                msg,
                                color = MaterialTheme.colorScheme.error.takeIf {
                                    loginUsernameError != null || loginPasswordError != null
                                } ?: MaterialTheme.colorScheme.onSurface
                            )
                        }

                        TextButton(onClick = { loginStatusMessage = "Password reset links are coming soon." }) {
                            Text("Forgot password?")
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Create an account",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Start tracking today with guided prompts and weekly insights.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = registerUsername,
                                onValueChange = {
                                    registerUsername = it
                                    registerUsernameError = null
                                },
                                label = { Text("Username") },
                                isError = registerUsernameError != null,
                                modifier = Modifier.fillMaxWidth()
                            )
                            registerUsernameError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }

                            OutlinedTextField(
                                value = registerEmail,
                                onValueChange = {
                                    registerEmail = it
                                    registerEmailError = null
                                },
                                label = { Text("Email") },
                                isError = registerEmailError != null,
                                modifier = Modifier.fillMaxWidth()
                            )
                            registerEmailError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }

                            OutlinedTextField(
                                value = registerPassword,
                                onValueChange = {
                                    registerPassword = it
                                    registerPasswordError = null
                                },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                isError = registerPasswordError != null,
                                modifier = Modifier.fillMaxWidth()
                            )
                            registerPasswordError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        ElevatedButton(
                            enabled = !isRegisterLoading,
                            onClick = {
                                if (isRegisterLoading) return@ElevatedButton

                                clearRegisterErrors()
                                registerStatusMessage = null
                                isRegisterLoading = true

                                scope.launch {
                                    try {
                                        when (
                                            val result = registerModel.register(
                                                RegisterInput(
                                                    username = registerUsername,
                                                    email = registerEmail,
                                                    password = registerPassword
                                                )
                                            )
                                        ) {
                                            is RegisterResult.ValidationError -> {
                                                applyRegisterValidation(result.validation)
                                            }

                                            is RegisterResult.Success -> {
                                                registerStatusMessage =
                                                    "Registrierung erfolgreich. Willkommen, ${result.user.username}!"
                                                onAuthenticated(result.loginResponse.userId)
                                            }

                                            is RegisterResult.Failure -> {
                                                registerStatusMessage =
                                                    result.message ?: "Registrierung fehlgeschlagen."
                                            }
                                        }
                                    } finally {
                                        isRegisterLoading = false
                                    }
                                }
                            }
                        ) {
                            Text("Register")
                        }

                        if (isRegisterLoading) {
                            CircularProgressIndicator()
                        }

                        registerStatusMessage?.let { msg ->
                            Text(
                                msg,
                                color = MaterialTheme.colorScheme.error.takeIf {
                                    registerUsernameError != null ||
                                        registerEmailError != null ||
                                        registerPasswordError != null
                                } ?: MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "By creating an account you agree to the terms and privacy policy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Need a quick overview?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "View your latest entries, mood trends, and reminders once you log in.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onNavigateToEntries) {
                        Text("See entries")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Support: support@moodtracker.app · Version 1.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
