package de.hsflensburg.moodtracker.android

import api.MoodTrackerClient

object MoodTrackerClientProvider {
    val client = MoodTrackerClient(AppConfig.BASE_URL)
}
