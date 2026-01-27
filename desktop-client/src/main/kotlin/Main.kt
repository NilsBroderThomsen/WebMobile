import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import api.MoodTrackerClient
import config.AppConfig
import dto.EntryDto
import views.*

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Mood Tracker") {
        App()
    }
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var currentUserId by remember { mutableStateOf<Long?>(null) }
    val client = remember { MoodTrackerClient(AppConfig.BASE_URL) }
    DisposableEffect(Unit) {
        onDispose {
            client.close()
        }
    }
    MaterialTheme {
        when (currentScreen) {
            Screen.Home -> {
                HomePage(
                    onNavigateToEntries = {
                        val userId = currentUserId
                        if (userId == null) {
                            currentScreen = Screen.Login
                        } else {
                            currentScreen = Screen.Entries(userId)
                        }
                    },
                    onNavigateToLogin = {
                        currentScreen = Screen.Login
                    },
                    onNavigateToRegister = {
                        currentScreen = Screen.Register
                    }
                )
            }
            Screen.Login -> {
                LoginViewPage(
                    client = client,
                    onNavigateBack = {
                        currentScreen = Screen.Home
                    },
                    onNavigateToEntries = { userId ->
                        currentUserId = userId
                        currentScreen = Screen.Entries(userId)
                    }
                )
            }
            Screen.Register -> {
                RegisterPage(
                    client = client,
                    onNavigateBack = {
                        currentScreen = Screen.Home
                    },
                    onNavigateToEntries = { userId ->
                        currentUserId = userId
                        currentScreen = Screen.Entries(userId)
                    }
                )
            }
            is Screen.Entries -> {
                val userId = (currentScreen as Screen.Entries).userId
                EntryListPage(
                    client = client,
                    userId = userId,
                    onNavigateBack = {
                        currentScreen = Screen.Home
                    },
                    onCreateEntry = {
                        currentScreen = Screen.CreateEntry(userId)
                    },
                    onUpdateEntry = { entryDto ->
                        currentScreen = Screen.UpdateEntry(entryDto)
                    },
                    onEntrySelected = { entryId ->
                        currentScreen = Screen.EntryDetails(entryId)
                    }
                )
            }
            is Screen.CreateEntry -> {
                CreateEntryPage(
                    client = client,
                    userId = (currentScreen as Screen.CreateEntry).userId,
                    onNavigateBack = {
                        val userId = currentUserId
                        if (userId == null) {
                            currentScreen = Screen.Home
                        } else {
                            currentScreen = Screen.Entries(userId)
                        }
                    }
                )
            }
            is Screen.UpdateEntry -> {
                UpdateEntryPage(
                    client = client,
                    entryDto = (currentScreen as Screen.UpdateEntry).entryDto, // Replace with actual entry ID
                    onNavigateBack = {
                        val userId = currentUserId
                        if (userId == null) {
                            currentScreen = Screen.Home
                        } else {
                            currentScreen = Screen.Entries(userId)
                        }
                    }
                )
            }
            is Screen.EntryDetails -> {
                EntryDetailsPage(
                    client = client,
                    entryId = (currentScreen as Screen.EntryDetails).entryId,
                    onNavigateBack = {
                        val userId = currentUserId
                        if (userId == null) {
                            currentScreen = Screen.Home
                        } else {
                            currentScreen = Screen.Entries(userId)
                        }
                    }
                )
            }
        }
    }
}

private sealed interface Screen {
    data object Home : Screen
    data object Login : Screen
    data object Register : Screen
    data class Entries(val userId: Long) : Screen
    data class CreateEntry(val userId: Long) : Screen
    data class UpdateEntry(val entryDto: EntryDto) : Screen
    data class EntryDetails(val entryId: Long) : Screen
}
