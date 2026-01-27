package de.hsflensburg.moodtracker.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import api.MoodTrackerClient
import config.AppConfig

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val client = remember { MoodTrackerClient(AppConfig.BASE_URL) }
            DisposableEffect(Unit) {
                onDispose {
                    client.close()
                }
            }
            MaterialTheme {
                App(client = client)
            }
        }
    }
}
