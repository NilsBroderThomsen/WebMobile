package de.hsflensburg.moodtracker.android

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dto.EntryDto
import kotlinx.coroutines.launch

class EntriesActivity : AppCompatActivity() {
    private val client = MoodTrackerClientProvider.client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entries)

        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        if (userId <= 0L) {
            Toast.makeText(this, getString(R.string.entries_missing_user), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val listView = findViewById<ListView>(R.id.entriesList)
        val emptyView = findViewById<TextView>(R.id.entriesEmpty)
        val loadingView = findViewById<ProgressBar>(R.id.entriesLoading)

        loadingView.visibility = View.VISIBLE
        listView.visibility = View.GONE
        emptyView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val entries = client.getEntries(userId)
                if (entries.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                } else {
                    listView.adapter = ArrayAdapter(
                        this@EntriesActivity,
                        android.R.layout.simple_list_item_1,
                        entries.map { entry -> formatEntry(entry) }
                    )
                    listView.visibility = View.VISIBLE
                }
            } catch (ex: Exception) {
                Toast.makeText(
                    this@EntriesActivity,
                    ex.message ?: getString(R.string.entries_load_failed),
                    Toast.LENGTH_LONG
                ).show()
                emptyView.visibility = View.VISIBLE
            } finally {
                loadingView.visibility = View.GONE
            }
        }
    }

    private fun formatEntry(entry: EntryDto): String {
        val mood = entry.moodRating?.let { getString(R.string.entries_mood_format, it) }
            ?: getString(R.string.entries_mood_unknown)
        return getString(
            R.string.entries_item_format,
            entry.title,
            mood,
            entry.createdAt
        )
    }

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
    }
}
