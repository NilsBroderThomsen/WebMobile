package de.hsflensburg.moodtracker.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import model.UpdateEntryInput
import model.UpdateEntryModel
import model.UpdateEntryResult

class EditEntryActivity : AppCompatActivity() {
    private val client = MoodTrackerClientProvider.client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_entry)

        val entryId = intent.getLongExtra(EXTRA_ID, INVALID_ID)
        val title = intent.getStringExtra(EXTRA_TITLE)
        val content = intent.getStringExtra(EXTRA_CONTENT)
        val moodRating = intent.getIntExtra(EXTRA_MOOD_RATING, MOOD_UNKNOWN)

        if (entryId == INVALID_ID || title == null || content == null) {
            Toast.makeText(this, getString(R.string.edit_entry_missing), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val titleInput = findViewById<EditText>(R.id.editEntryName)
        val contentInput = findViewById<EditText>(R.id.editEntryContent)
        val moodInput = findViewById<EditText>(R.id.editEntryMood)
        val saveButton = findViewById<Button>(R.id.editEntrySave)
        val updateEntryModel = UpdateEntryModel(client)

        titleInput.setText(title)
        contentInput.setText(content)
        if (moodRating != MOOD_UNKNOWN) {
            moodInput.setText(moodRating.toString())
        }

        saveButton.setOnClickListener {
            val input = UpdateEntryInput(
                title = titleInput.text.toString(),
                content = contentInput.text.toString(),
                moodRatingInput = moodInput.text.toString()
            )
            saveButton.isEnabled = false
            lifecycleScope.launch {
                when (val result = updateEntryModel.updateEntry(entryId, input)) {
                    is UpdateEntryResult.Success -> {
                        Toast.makeText(
                            this@EditEntryActivity,
                            getString(R.string.edit_entry_success),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    is UpdateEntryResult.ValidationError -> {
                        val message = updateEntryModel.validationMessage(result.validation)
                            ?: getString(R.string.edit_entry_validation_failed)
                        Toast.makeText(this@EditEntryActivity, message, Toast.LENGTH_LONG).show()
                    }
                    is UpdateEntryResult.Failure -> {
                        Toast.makeText(
                            this@EditEntryActivity,
                            result.message ?: getString(R.string.edit_entry_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                saveButton.isEnabled = true
            }
        }
    }

    companion object {
        private const val EXTRA_ID = "extra_entry_id"
        private const val EXTRA_TITLE = "extra_entry_title"
        private const val EXTRA_CONTENT = "extra_entry_content"
        private const val EXTRA_MOOD_RATING = "extra_entry_mood_rating"
        private const val MOOD_UNKNOWN = -1
        private const val INVALID_ID = -1L

        fun newIntent(
            context: Context,
            entryId: Long,
            title: String,
            content: String,
            moodRating: Int?
        ): Intent {
            return Intent(context, EditEntryActivity::class.java).apply {
                putExtra(EXTRA_ID, entryId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_CONTENT, content)
                putExtra(EXTRA_MOOD_RATING, moodRating ?: MOOD_UNKNOWN)
            }
        }
    }
}
