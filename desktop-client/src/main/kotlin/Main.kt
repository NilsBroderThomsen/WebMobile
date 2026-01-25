import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Hello Compose") {
        App()
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    MaterialTheme {
        NavHost(
            navController = navController,
            startDestination = HomePage
        ) {
            composable<HomePage> {
                StudentsList(
                    onNavigateToSettings = {
                        navController.navigate(SettingsPage)
                    }
                )
            }
            composable<SettingsPage> {
                SettingsView(
                    onNavigateBack = {
                        navController.navigate(HomePage)
                    }
                )
            }
        }
    }
}

@Serializable
object HomePage

@Serializable
object SettingsPage
