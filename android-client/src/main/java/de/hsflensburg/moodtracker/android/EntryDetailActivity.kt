package de.hsflensburg.moodtracker.android

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dto.EntryDto
import extension.displayMood
import extension.toDisplayTimestamp
import kotlinx.coroutines.launch

class EntryDetailActivity : AppCompatActivity() {
    private val client = MoodTrackerClientProvider.client
    private var entryId: Long = INVALID_ID
    private var currentEntry: EntryDto? = null
    private lateinit var titleView: TextView
    private lateinit var contentView: TextView
    private lateinit var moodView: TextView
    private lateinit var createdAtView: TextView
    private lateinit var updatedAtView: TextView
    private lateinit var tagsView: TextView
    private lateinit var contentContainer: View
    private lateinit var skeletonContainer: View
    private var skeletonAnimator: ObjectAnimator? = null

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
        contentContainer = findViewById(R.id.entryDetailContentContainer)
        skeletonContainer = findViewById(R.id.entryDetailSkeleton)

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
            val entry = currentEntry
            val editTitle = entry?.title ?: title
            val editContent = entry?.content ?: content
            val editMoodRating = entry?.moodRating ?: if (moodRating == MOOD_UNKNOWN) null else moodRating
            if (editTitle.isBlank() || editContent.isBlank()) {
                Toast.makeText(
                    this@EntryDetailActivity,
                    getString(R.string.entry_detail_missing),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            startActivity(
                UpdateEntryActivity.newIntent(
                    this@EntryDetailActivity,
                    entryId = entryId,
                    title = editTitle,
                    content = editContent,
                    moodRating = editMoodRating
                )
            )
        }

        titleView.text = title
        contentView.text = content
        moodView.text = displayMood(
            moodRating = if (moodRating == MOOD_UNKNOWN) null else moodRating,
            unknownText = getString(R.string.entries_mood_unknown),
            formatRating = { rating -> getString(R.string.entries_mood_format, rating) }
        )
        createdAtView.text = createdAt.toDisplayTimestamp()
        updatedAtView.text = updatedAt?.toDisplayTimestamp()
            ?: getString(R.string.entry_detail_not_updated)
        tagsView.text = if (tags.isNullOrEmpty()) {
            getString(R.string.entry_detail_no_tags)
        } else {
            tags.joinToString(", ")
        }
    }

    override fun onResume() {
        super.onResume()
        loadEntryDetails()
    }

    private fun loadEntryDetails() {
        if (entryId == INVALID_ID) {
            return
        }
        setLoading(true)
        lifecycleScope.launch {
            try {
                val entry = client.getEntryDetails(entryId)
                currentEntry = entry
                titleView.text = entry.title
                contentView.text = entry.content
                moodView.text = entry.displayMood(
                    unknownText = getString(R.string.entries_mood_unknown),
                    formatRating = { rating -> getString(R.string.entries_mood_format, rating) }
                )
                createdAtView.text = entry.createdAt.toDisplayTimestamp()
                updatedAtView.text = entry.updatedAt?.toDisplayTimestamp() ?: getString(R.string.entry_detail_not_updated)
                tagsView.text = if (entry.tags.isEmpty()) {
                    getString(R.string.entry_detail_no_tags)
                } else {
                    entry.tags.joinToString(", ")
                }
            } catch (ex: Exception) {
                Toast.makeText(
                    this@EntryDetailActivity,
                    ex.message ?: getString(R.string.entries_load_failed),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            contentContainer.visibility = View.INVISIBLE
            skeletonContainer.visibility = View.VISIBLE
            if (skeletonAnimator == null) {
                skeletonAnimator = ObjectAnimator.ofFloat(skeletonContainer, View.ALPHA, 0.4f, 1f).apply {
                    duration = 800
                    repeatMode = ValueAnimator.REVERSE
                    repeatCount = ValueAnimator.INFINITE
                }
            }
            skeletonAnimator?.start()
        } else {
            contentContainer.visibility = View.VISIBLE
            skeletonContainer.visibility = View.GONE
            skeletonAnimator?.cancel()
            skeletonContainer.alpha = 1f
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
