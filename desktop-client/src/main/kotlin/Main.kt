import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import api.MoodTrackerClient
import dto.EntryDto

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

    data object Entries : Screen
}

@Composable
fun HomeView(onNavigateToEntries: () -> Unit) {
    Button(onClick = onNavigateToEntries) {
        Text("My Entries")
    }
}

@Composable
fun EntryList(userId: Long, onNavigateBack: () -> Unit) {
    val baseUrl = "http://localhost:8080"
    var entries by remember { mutableStateOf<List<EntryDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        val client = MoodTrackerClient(baseUrl)
        entries = client.getEntries(userId)
    }

    Column {
        Button(onClick = onNavigateBack) {
            Text("Back")
        }

        entries.forEach { entry ->
            Text("Entry ID: ${entry.id}, Title: ${entry.title}, Content: ${entry.content}, Mood: ${entry.moodRating}")
        }
    }
}
