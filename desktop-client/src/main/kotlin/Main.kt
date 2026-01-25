import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import api.MoodTrackerClient
import dto.EntryDto
import kotlinx.serialization.Serializable

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Mood Tracker") {
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
                HomeView(
                    onNavigateToEntries = {
                        navController.navigate(EntriesPage)
                    }
                )
            }
            composable<EntriesPage> {
                EntryList(
                    userId = 1L,
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

@Composable
fun HomeView(onNavigateToEntries: () -> Unit) {
    Button(onClick = onNavigateToEntries) {
        Text("My Entries")
    }
}

@Serializable
object EntriesPage

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
