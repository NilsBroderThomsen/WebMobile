package de.hsflensburg.moodtracker.android

import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
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
                val entries = client.getEntries(userId).toMutableList()
                if (entries.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                } else {
                    listView.adapter = ArrayAdapter(
                        this@EntriesActivity,
                        R.layout.entry_list_item,
                        R.id.entryText,
                        entries
                    ) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val rowView = convertView ?: LayoutInflater.from(context).inflate(
                                R.layout.entry_list_item,
                                parent,
                                false
                            )
                            val entry = getItem(position)
                            val entryText = rowView.findViewById<TextView>(R.id.entryText)
                            val deleteButton = rowView.findViewById<Button>(R.id.entryDelete)

                            if (entry != null) {
                                entryText.text = formatEntry(entry)
                                deleteButton.setOnClickListener {
                                    lifecycleScope.launch {
                                        try {
                                            client.deleteEntry(entry.id)
                                            remove(entry)
                                            notifyDataSetChanged()
                                            if (isEmpty) {
                                                listView.visibility = View.GONE
                                                emptyView.visibility = View.VISIBLE
                                            }
                                        } catch (ex: Exception) {
                                            Toast.makeText(
                                                this@EntriesActivity,
                                                ex.message ?: getString(R.string.entries_delete_failed),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            } else {
                                entryText.text = ""
                                deleteButton.setOnClickListener(null)
                            }

                            return rowView
                        }
                    }
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
