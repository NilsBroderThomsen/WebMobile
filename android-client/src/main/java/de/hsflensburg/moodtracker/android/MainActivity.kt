package de.hsflensburg.moodtracker.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    MaterialTheme {
        Scaffold(
            topBar
            = {
                TopAppBar(
                    title
                )
                = { Text("Student Management") }
            },
            bottomBar = {
                BottomAppBar {
                    Text("Bottom Bar")
                }
            }
        ) { innerPadding ->
            Column(
                modifier
                = Modifier.padding(innerPadding)
            ) {
                // Content here
            }
        }
    }
}