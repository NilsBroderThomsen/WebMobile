import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Mood Tracker") {
        App()
    }
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    MaterialTheme {
        when (currentScreen) {
            Screen.Home -> {
                HomeView(
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
                LoginView(
                    onNavigateBack = {
                        currentScreen = Screen.Home
                    },
                    onNavigateToEntries = {
                        currentScreen = Screen.Entries
                    }
                )
            }
            Screen.Register -> {
                RegisterView(
                    onNavigateBack = {
                        currentScreen = Screen.Home
                    },
                    onNavigateToEntries = {
                        currentScreen = Screen.Entries
                    }
                )
            }
            Screen.Entries -> {
                EntryList(
                    userId = 1L,
                    onNavigateBack = {
                        currentScreen = Screen.Home
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
}
