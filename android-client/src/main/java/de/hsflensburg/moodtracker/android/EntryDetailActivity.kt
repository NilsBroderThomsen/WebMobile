package de.hsflensburg.moodtracker.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dto.EntryDto

class EntryDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_detail)

        val title = intent.getStringExtra(EXTRA_TITLE)
        val content = intent.getStringExtra(EXTRA_CONTENT)
        val createdAt = intent.getStringExtra(EXTRA_CREATED_AT)
        val updatedAt = intent.getStringExtra(EXTRA_UPDATED_AT)
        val moodRating = intent.getIntExtra(EXTRA_MOOD_RATING, MOOD_UNKNOWN)
        val tags = intent.getStringArrayListExtra(EXTRA_TAGS)

        if (title.isNullOrBlank() || content.isNullOrBlank() || createdAt.isNullOrBlank()) {
            Toast.makeText(this, getString(R.string.entry_detail_missing), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        findViewById<TextView>(R.id.entryDetailTitle).text = title
        findViewById<TextView>(R.id.entryDetailContent).text = content
        findViewById<TextView>(R.id.entryDetailMood).text = if (moodRating == MOOD_UNKNOWN) {
            getString(R.string.entries_mood_unknown)
        } else {
            getString(R.string.entries_mood_format, moodRating)
        }
        findViewById<TextView>(R.id.entryDetailCreatedAt).text = createdAt
        findViewById<TextView>(R.id.entryDetailUpdatedAt).text = updatedAt
            ?: getString(R.string.entry_detail_not_updated)
        findViewById<TextView>(R.id.entryDetailTags).text = if (tags.isNullOrEmpty()) {
            getString(R.string.entry_detail_no_tags)
        } else {
            tags.joinToString(", ")
        }
    }

    companion object {
        private const val EXTRA_TITLE = "extra_entry_title"
        private const val EXTRA_CONTENT = "extra_entry_content"
        private const val EXTRA_MOOD_RATING = "extra_entry_mood_rating"
        private const val EXTRA_CREATED_AT = "extra_entry_created_at"
        private const val EXTRA_UPDATED_AT = "extra_entry_updated_at"
        private const val EXTRA_TAGS = "extra_entry_tags"
        private const val MOOD_UNKNOWN = -1

        fun newIntent(context: Context, entry: EntryDto): Intent {
            return Intent(context, EntryDetailActivity::class.java).apply {
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
