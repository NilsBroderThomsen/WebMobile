package de.hsflensburg.moodtracker.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dto.EntryDto
import extension.formatEntryTimestamp
import extension.toEmoji
import kotlinx.coroutines.launch

class EntriesActivity : AppCompatActivity() {
    private val client = MoodTrackerClientProvider.client
    private var currentEntries: List<EntryDto> = emptyList()
    private var isAscending = true
    private var userId: Long = -1L
    private lateinit var listView: ListView
    private lateinit var emptyView: TextView
    private lateinit var loadingView: ProgressBar
    private lateinit var sortToggle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entries)

        userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        if (userId <= 0L) {
            Toast.makeText(this, getString(R.string.entries_missing_user), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        listView = findViewById(R.id.entriesList)
        emptyView = findViewById(R.id.entriesEmpty)
        loadingView = findViewById(R.id.entriesLoading)
        sortToggle = findViewById(R.id.entriesSortToggle)
        val createButton = findViewById<Button>(R.id.entriesCreateButton)
        val logoutButton = findViewById<Button>(R.id.entriesLogoutButton)

        createButton.setOnClickListener {
            startActivity(CreateEntryActivity.newIntent(this, userId))
        }

        logoutButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }

        sortToggle.setOnClickListener {
            isAscending = !isAscending
            updateSortToggle(sortToggle)
            renderEntries(listView, currentEntries, isAscending)
        }
    }

    override fun onResume() {
        super.onResume()
        loadEntries()
    }

    private fun loadEntries() {
        loadingView.visibility = View.VISIBLE
        listView.visibility = View.GONE
        emptyView.visibility = View.GONE
        sortToggle.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val entries = client.getEntries(userId)
                if (entries.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                } else {
                    currentEntries = entries
                    isAscending = true
                    updateSortToggle(sortToggle)
                    sortToggle.visibility = View.VISIBLE
                    renderEntries(listView, currentEntries, isAscending)
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

    private fun renderEntries(listView: ListView, entries: List<EntryDto>, ascending: Boolean) {
        val sortedEntries = if (ascending) {
            entries.sortedBy { it.createdAt }
        } else {
            entries.sortedByDescending { it.createdAt }
        }
        listView.adapter = EntriesAdapter(this, sortedEntries)
        listView.setOnItemClickListener { _, _, position, _ ->
            val entry = sortedEntries[position]
            val intent = EntryDetailActivity.newIntent(this, entry)
            startActivity(intent)
        }
    }

    private fun updateSortToggle(sortToggle: Button) {
        sortToggle.setText(
            if (isAscending) R.string.entries_sort_oldest else R.string.entries_sort_newest
        )
    }

    private fun formatMood(entry: EntryDto): String {
        return entry.moodRating?.let { rating ->
            "${getString(R.string.entries_mood_format, rating)} ${rating.toEmoji()}"
        } ?: getString(R.string.entries_mood_unknown)
    }

    private class EntriesAdapter(
        activity: EntriesActivity,
        entries: List<EntryDto>
    ) : android.widget.ArrayAdapter<EntryDto>(activity, R.layout.list_item_entry, entries) {
        private val inflater = activity.layoutInflater

        override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
            val view = convertView ?: inflater.inflate(R.layout.list_item_entry, parent, false)
            val entry = getItem(position) ?: return view
            val titleView = view.findViewById<TextView>(R.id.entryTitle)
            val timestampView = view.findViewById<TextView>(R.id.entryTimestamp)
            val mood = (context as EntriesActivity).formatMood(entry)
            titleView.text = context.getString(
                R.string.entries_item_title_format,
                entry.title,
                mood
            )
            timestampView.text = entry.createdAt.formatEntryTimestamp()
            return view
        }
    }

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
    }
}
