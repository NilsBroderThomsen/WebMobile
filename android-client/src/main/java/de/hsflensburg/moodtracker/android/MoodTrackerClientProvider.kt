package de.hsflensburg.moodtracker.android

import api.MoodTrackerClient
import session.AuthSession

object MoodTrackerClientProvider {
    val client = MoodTrackerClient(AppConfig.BASE_URL)
    val session = AuthSession(client)
}
