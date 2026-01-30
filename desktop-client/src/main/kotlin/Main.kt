import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import api.MoodTrackerClient
import config.AppConfig
import dto.EntryDto
import views.*

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Mood Tracker",
        state = rememberWindowState(size = DpSize(800.dp, 700.dp))) {
        App()
    }
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Landing) }
    var authUserId by remember { mutableStateOf<Long?>(null) }
    val client = remember { MoodTrackerClient(AppConfig.BASE_URL) }
    DisposableEffect(Unit) {
        onDispose {
            client.close()
        }
    }
    MaterialTheme {
        when (currentScreen) {
            Screen.Landing -> {
                LandingPage(
                    client = client,
                    onLoginSuccess = { userId ->
                        authUserId = userId
                        currentScreen = Screen.Entries
                    },
                    onRegisterSuccess = { userId ->
                        authUserId = userId
                        currentScreen = Screen.Entries
                    }
                )
            }
            Screen.Home -> {
                val userId = authUserId
                if (userId == null) {
                    LaunchedEffect(Unit) {
                        currentScreen = Screen.Landing
                    }
                } else {
                    HomePage(
                        onNavigateToEntries = {
                            currentScreen = Screen.Entries
                        },
                        onNavigateToCreateEntry = {
                            currentScreen = Screen.CreateEntry
                        },
                        onLogout = {
                            client.logout()
                            authUserId = null
                            currentScreen = Screen.Landing
                        }
                    )
                }
            }
            Screen.Entries -> {
                val userId = authUserId
                if (userId == null) {
                    LaunchedEffect(Unit) {
                        currentScreen = Screen.Landing
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
                        currentScreen = Screen.Landing
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
    data object Landing : Screen
    data object Home : Screen
    data object Entries : Screen
    data object CreateEntry : Screen
    data class UpdateEntry(val entryDto: EntryDto) : Screen
    data class EntryDetails(val entryId: Long) : Screen
}
