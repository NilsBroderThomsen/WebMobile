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
    var authUserId by remember { mutableStateOf<Long?>(null) }
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
                        currentScreen = if (authUserId == null) {
                            Screen.Login
                        } else {
                            Screen.Entries
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
                        authUserId = userId
                        currentScreen = Screen.Entries
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
                        authUserId = userId
                        currentScreen = Screen.Entries
                    }
                )
            }
            Screen.Entries -> {
                val userId = authUserId
                if (userId == null) {
                    LaunchedEffect(Unit) {
                        currentScreen = Screen.Login
                    }
                } else {
                    EntryListPage(
                        client = client,
                        userId = userId,
                        onNavigateBack = {
                            currentScreen = Screen.Home
                        },
                        onCreateEntry = {
                            currentScreen = Screen.CreateEntry
                        },
                        onUpdateEntry = { entryDto ->
                            currentScreen = Screen.UpdateEntry(entryDto)
                        },
                        onEntrySelected = { entryId ->
                            currentScreen = Screen.EntryDetails(entryId)
                        }
                    )
                }
            }
            Screen.CreateEntry -> {
                val userId = authUserId
                if (userId == null) {
                    LaunchedEffect(Unit) {
                        currentScreen = Screen.Login
                    }
                } else {
                    CreateEntryPage(
                        client = client,
                        userId = userId,
                        onNavigateBack = {
                            currentScreen = Screen.Entries
                        }
                    )
                }
            }
            is Screen.UpdateEntry -> {
                UpdateEntryPage(
                    client = client,
                    entryDto = (currentScreen as Screen.UpdateEntry).entryDto, // Replace with actual entry ID
                    onNavigateBack = {
                        currentScreen = Screen.Entries
                    }
                )
            }
            is Screen.EntryDetails -> {
                EntryDetailsPage(
                    client = client,
                    entryId = (currentScreen as Screen.EntryDetails).entryId,
                    onNavigateBack = {
                        currentScreen = Screen.Entries
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
    data object Entries : Screen
    data object CreateEntry : Screen
    data class UpdateEntry(val entryDto: EntryDto) : Screen
    data class EntryDetails(val entryId: Long) : Screen
}
