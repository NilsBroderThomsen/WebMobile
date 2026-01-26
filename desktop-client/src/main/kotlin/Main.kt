import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import api.MoodTrackerClient
import config.AppConfig
import views.CreateEntryPage
import views.EntryListPage
import views.HomePage
import views.LoginViewPage
import views.RegisterPage

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
                    }
                )
            }
            Screen.CreateEntry -> {
                CreateEntryPage(
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
}
