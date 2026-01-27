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
                        currentScreen = Screen.Entries
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
                    onNavigateToEntries = {
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
                    onNavigateToEntries = {
                        currentScreen = Screen.Entries
                    }
                )
            }
            Screen.Entries -> {
                EntryListPage(
                    client = client,
                    userId = 1L,
                    onNavigateBack = {
                        currentScreen = Screen.Home
                    },
                    onCreateEntry = {
                        currentScreen = Screen.CreateEntry
                    },
                    onUpdateEntry = { entryDto ->
                        currentScreen = Screen.UpdateEntry(entryDto)
                    }
                )
            }
            Screen.CreateEntry -> {
                CreateEntryPage(
                    client = client,
                    onNavigateBack = {
                        currentScreen = Screen.Entries
                    }
                )
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
}
