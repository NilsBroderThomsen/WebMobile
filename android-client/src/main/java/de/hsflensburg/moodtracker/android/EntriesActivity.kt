package de.hsflensburg.moodtracker.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dto.EntryDto
import extension.EntrySortOrder
import extension.displayMood
import extension.sortedByCreatedAt
import extension.toDisplayTimestamp
import kotlinx.coroutines.launch

class EntriesActivity : AppCompatActivity() {
    private val client = MoodTrackerClientProvider.client
    private val session = MoodTrackerClientProvider.session
    private var currentEntries: List<EntryDto> = emptyList()
    private var isAscending = true
    private var activeFilter = MoodFilter.NONE
    private var userId: Long = -1L
    private lateinit var listView: ListView
    private lateinit var loadingListView: ListView
    private lateinit var emptyView: TextView
    private lateinit var sortToggle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entries)

        userId = session.authenticatedUserId ?: -1L
        if (userId <= 0L) {
            Toast.makeText(this, getString(R.string.entries_missing_user), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        listView = findViewById(R.id.entriesList)
        loadingListView = findViewById(R.id.entriesLoadingContainer)
        emptyView = findViewById(R.id.entriesEmpty)
        sortToggle = findViewById(R.id.entriesSortToggle)
        val createButton = findViewById<Button>(R.id.entriesCreateButton)
        val logoutButton = findViewById<Button>(R.id.entriesLogoutButton)
        val skeletonRows = List(6) { it }
        loadingListView.adapter = SkeletonEntriesAdapter(this, skeletonRows)

        createButton.setOnClickListener {
            startActivity(CreateEntryActivity.newIntent(this))
        }

        logoutButton.setOnClickListener {
            session.logout()
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }

        sortToggle.setOnClickListener { view ->
            showSortFilterMenu(view)
        }
    }

    override fun onResume() {
        super.onResume()
        loadEntries()
    }

    private fun loadEntries() {
        loadingListView.visibility = View.VISIBLE
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
                    activeFilter = MoodFilter.NONE
                    updateSortToggle(sortToggle)
                    sortToggle.visibility = View.VISIBLE
                    renderEntries(listView, currentEntries, isAscending, activeFilter)
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
                loadingListView.visibility = View.GONE
            }
        }
    }

    private fun renderEntries(
        listView: ListView,
        entries: List<EntryDto>,
        ascending: Boolean,
        filter: MoodFilter
    ) {
        val filteredEntries = entries.filter { entry ->
            filter.matches(entry.moodRating)
        }
        val sortOrder = if (ascending) EntrySortOrder.ASC else EntrySortOrder.DESC
        val sortedEntries = filteredEntries.sortedByCreatedAt(sortOrder)

        listView.adapter = EntriesAdapter(this, sortedEntries)
        listView.setOnItemClickListener { _, _, position, _ ->
            val entry = sortedEntries[position]
            val intent = EntryDetailActivity.newIntent(this, entry)
            startActivity(intent)
        }
    }

    private fun updateSortToggle(sortToggle: Button) {
        val sortLabel =
            if (isAscending) getString(R.string.entries_sort_oldest) else getString(R.string.entries_sort_newest)
        val filterLabel = getString(activeFilter.labelResId)
        sortToggle.text = getString(R.string.entries_sort_filter_status, sortLabel, filterLabel)
    }

    private fun showSortFilterMenu(anchor: View) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.entries_sort_filter_menu, popupMenu.menu)
        popupMenu.menu.setGroupCheckable(R.id.entriesSortGroup, true, true)
        popupMenu.menu.setGroupCheckable(R.id.entriesFilterGroup, true, true)

        val sortItemId = if (isAscending) {
            R.id.entriesSortOldest
        } else {
            R.id.entriesSortNewest
        }
        popupMenu.menu.findItem(sortItemId)?.isChecked = true
        popupMenu.menu.findItem(activeFilter.menuId)?.isChecked = true

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.entriesSortNewest -> {
                    isAscending = false
                }
                R.id.entriesSortOldest -> {
                    isAscending = true
                }
                R.id.entriesFilterHigh -> {
                    activeFilter = MoodFilter.HIGH
                }
                R.id.entriesFilterMid -> {
                    activeFilter = MoodFilter.MID
                }
                R.id.entriesFilterLow -> {
                    activeFilter = MoodFilter.LOW
                }
                R.id.entriesFilterNone -> {
                    activeFilter = MoodFilter.NONE
                }
            }
            updateSortToggle(sortToggle)
            renderEntries(listView, currentEntries, isAscending, activeFilter)
            true
        }
        popupMenu.show()
    }

    private fun formatMood(entry: EntryDto): String {
        return entry.displayMood(
            unknownText = getString(R.string.entries_mood_unknown),
            formatRating = { rating -> getString(R.string.entries_mood_format, rating) }
        )
    }

    private enum class MoodFilter(
        val menuId: Int,
        val labelResId: Int,
        val matcher: (Int?) -> Boolean
    ) {
        NONE(R.id.entriesFilterNone, R.string.entries_filter_none, { true }),
        HIGH(R.id.entriesFilterHigh, R.string.entries_filter_high, { rating -> rating != null && rating >= 8 }),
        MID(
            R.id.entriesFilterMid,
            R.string.entries_filter_mid,
            { rating -> rating != null && rating in 5..7 }
        ),
        LOW(R.id.entriesFilterLow, R.string.entries_filter_low, { rating -> rating != null && rating <= 4 });

        fun matches(rating: Int?): Boolean = matcher(rating)
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
            timestampView.text = entry.createdAt.toDisplayTimestamp()
            return view
        }
    }

    private class SkeletonEntriesAdapter(
        activity: EntriesActivity,
        entries: List<Int>
    ) : android.widget.ArrayAdapter<Int>(activity, R.layout.list_item_entry_skeleton, entries) {
        private val inflater = activity.layoutInflater

        override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
            return convertView ?: inflater.inflate(R.layout.list_item_entry_skeleton, parent, false)
        }
    }

}
