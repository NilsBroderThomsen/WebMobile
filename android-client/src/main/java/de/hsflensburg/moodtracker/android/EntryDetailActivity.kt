package de.hsflensburg.moodtracker.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dto.EntryDto
import extension.toEmoji
import kotlinx.coroutines.launch

class EntryDetailActivity : AppCompatActivity() {
    private val client = MoodTrackerClientProvider.client
    private var entryId: Long = INVALID_ID
    private var currentTitle: String = ""
    private var currentContent: String = ""
    private var currentMoodRating: Int? = null
    private lateinit var titleView: TextView
    private lateinit var contentView: TextView
    private lateinit var moodView: TextView
    private lateinit var createdAtView: TextView
    private lateinit var updatedAtView: TextView
    private lateinit var tagsView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_detail)

        entryId = intent.getLongExtra(EXTRA_ID, INVALID_ID)
        val title = intent.getStringExtra(EXTRA_TITLE)
        val content = intent.getStringExtra(EXTRA_CONTENT)
        val createdAt = intent.getStringExtra(EXTRA_CREATED_AT)
        val updatedAt = intent.getStringExtra(EXTRA_UPDATED_AT)
        val moodRating = intent.getIntExtra(EXTRA_MOOD_RATING, MOOD_UNKNOWN)
        val tags = intent.getStringArrayListExtra(EXTRA_TAGS)

        if (entryId == INVALID_ID || title.isNullOrBlank() || content.isNullOrBlank() || createdAt.isNullOrBlank()) {
            Toast.makeText(this, getString(R.string.entry_detail_missing), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        titleView = findViewById(R.id.entryDetailTitle)
        contentView = findViewById(R.id.entryDetailContent)
        moodView = findViewById(R.id.entryDetailMood)
        createdAtView = findViewById(R.id.entryDetailCreatedAt)
        updatedAtView = findViewById(R.id.entryDetailUpdatedAt)
        tagsView = findViewById(R.id.entryDetailTags)

        findViewById<Button>(R.id.entryDetailBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.entryDetailDelete).setOnClickListener {
            lifecycleScope.launch {
                try {
                    client.deleteEntry(entryId)
                    Toast.makeText(
                        this@EntryDetailActivity,
                        getString(R.string.entry_detail_delete_success),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } catch (ex: Exception) {
                    Toast.makeText(
                        this@EntryDetailActivity,
                        ex.message ?: getString(R.string.entry_detail_delete_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        findViewById<Button>(R.id.entryDetailEdit).setOnClickListener {
            startActivity(
                EditEntryActivity.newIntent(
                    this@EntryDetailActivity,
                    entryId = entryId,
                    title = currentTitle,
                    content = currentContent,
                    moodRating = currentMoodRating
                )
            )
        }

        renderEntry(
            title = title,
            content = content,
            moodRating = if (moodRating == MOOD_UNKNOWN) null else moodRating,
            createdAt = createdAt,
            updatedAt = updatedAt,
            tags = tags
        )
    }

    override fun onResume() {
        super.onResume()
        if (entryId == INVALID_ID) {
            return
        }
        lifecycleScope.launch {
            try {
                val entry = client.getEntryDetails(entryId)
                renderEntry(
                    title = entry.title,
                    content = entry.content,
                    moodRating = entry.moodRating,
                    createdAt = entry.createdAt,
                    updatedAt = entry.updatedAt,
                    tags = entry.tags
                )
            } catch (ex: Exception) {
                Toast.makeText(
                    this@EntryDetailActivity,
                    ex.message ?: getString(R.string.entries_load_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun renderEntry(
        title: String,
        content: String,
        moodRating: Int?,
        createdAt: String,
        updatedAt: String?,
        tags: List<String>?
    ) {
        currentTitle = title
        currentContent = content
        currentMoodRating = moodRating
        titleView.text = title
        contentView.text = content
        moodView.text = if (moodRating == null) {
            getString(R.string.entries_mood_unknown)
        } else {
            "${getString(R.string.entries_mood_format, moodRating)} ${moodRating.toEmoji()}"
        }
        createdAtView.text = createdAt
        updatedAtView.text = updatedAt ?: getString(R.string.entry_detail_not_updated)
        tagsView.text = if (tags.isNullOrEmpty()) {
            getString(R.string.entry_detail_no_tags)
        } else {
            tags.joinToString(", ")
        }
    }

    companion object {
        private const val EXTRA_ID = "extra_entry_id"
        private const val EXTRA_TITLE = "extra_entry_title"
        private const val EXTRA_CONTENT = "extra_entry_content"
        private const val EXTRA_MOOD_RATING = "extra_entry_mood_rating"
        private const val EXTRA_CREATED_AT = "extra_entry_created_at"
        private const val EXTRA_UPDATED_AT = "extra_entry_updated_at"
        private const val EXTRA_TAGS = "extra_entry_tags"
        private const val MOOD_UNKNOWN = -1
        private const val INVALID_ID = -1L

        fun newIntent(context: Context, entry: EntryDto): Intent {
            return Intent(context, EntryDetailActivity::class.java).apply {
                putExtra(EXTRA_ID, entry.id)
                putExtra(EXTRA_TITLE, entry.title)
                putExtra(EXTRA_CONTENT, entry.content)
                putExtra(EXTRA_MOOD_RATING, entry.moodRating ?: MOOD_UNKNOWN)
                putExtra(EXTRA_CREATED_AT, entry.createdAt)
                putExtra(EXTRA_UPDATED_AT, entry.updatedAt)
                putStringArrayListExtra(EXTRA_TAGS, ArrayList(entry.tags))
            }
        }
    }
}
