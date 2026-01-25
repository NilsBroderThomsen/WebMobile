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
import api.MoodTrackerClient
import dto.EntryDto

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
                HomeView()
            }
            composable<EntriesPage> {
                EntryList(1L)
            }
        }
    }
}

@Serializable
object HomePage

@Composable
fun HomeView() {
    Button(onClick = { /*TODO*/ }) {
        Text("My Entries")
    }
}

@Serializable
object EntriesPage

@Composable
fun EntryList(userId: Long) {
    val baseUrl = "http://localhost:8080"
    var entries by remember { mutableStateOf<List<EntryDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        val client = MoodTrackerClient(baseUrl)
        entries = client.getEntries(userId)
    }

    Column {
        entries.forEach { entry ->
            Text("Entry ID: ${entry.id}, Title: ${entry.title}, Content: ${entry.content}, Mood: ${entry.moodRating}")
        }
    }
}

