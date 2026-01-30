package de.hsflensburg.moodtracker.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import model.CreateEntryInput
import model.CreateEntryModel
import model.CreateEntryResult

class CreateEntryActivity : AppCompatActivity() {
    private val client = MoodTrackerClientProvider.client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_entry)

        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        if (userId <= 0L) {
            Toast.makeText(this, getString(R.string.entries_missing_user), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val titleLayout = findViewById<TextInputLayout>(R.id.createEntryNameLayout)
        val contentLayout = findViewById<TextInputLayout>(R.id.createEntryContentLayout)
        val moodLayout = findViewById<TextInputLayout>(R.id.createEntryMoodLayout)

        val titleInput = findViewById<EditText>(R.id.createEntryName)
        val contentInput = findViewById<EditText>(R.id.createEntryContent)
        val moodInput = findViewById<EditText>(R.id.createEntryMood)

        val backButton = findViewById<Button>(R.id.createEntryBack)
        val saveButton = findViewById<Button>(R.id.createEntrySave)

        val createEntryModel = CreateEntryModel(client)

        fun clearErrors() {
            titleLayout.error = null
            contentLayout.error = null
            moodLayout.error = null
        }

        fun applyValidationErrors(validation: model.CreateEntryValidation) {
            titleLayout.error = when {
                validation.missingTitle -> getString(R.string.error_required_field)
                else -> null
            }
            contentLayout.error = when {
                validation.missingContent -> getString(R.string.error_required_field)
                else -> null
            }
            moodLayout.error = when {
                validation.invalidMoodFormat -> getString(R.string.error_invalid_mood_format) // z.B. "Bitte Zahl eingeben"
                validation.moodOutOfRange -> getString(R.string.error_invalid_mood_range) // z.B. "1 bis 10"
                else -> null
            }
        }

        // Fehler weg, sobald User tippt (UX = nicht sad)
        titleInput.addTextChangedListener { titleLayout.error = null }
        contentInput.addTextChangedListener { contentLayout.error = null }
        moodInput.addTextChangedListener { moodLayout.error = null }

        backButton.setOnClickListener { finish() }

        saveButton.setOnClickListener {
            if (!saveButton.isEnabled) return@setOnClickListener

            clearErrors()

            val input = CreateEntryInput(
                title = titleInput.text?.toString().orEmpty(),
                content = contentInput.text?.toString().orEmpty(),
                moodRatingInput = moodInput.text?.toString().orEmpty()
            )

            saveButton.isEnabled = false
            lifecycleScope.launch {
                try {
                    when (val result = createEntryModel.createEntry(userId, input)) {
                        is CreateEntryResult.Success -> {
                            Toast.makeText(
                                this@CreateEntryActivity,
                                getString(R.string.create_entry_success),
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                        is CreateEntryResult.ValidationError -> {
                            applyValidationErrors(result.validation)
                            Toast.makeText(
                                this@CreateEntryActivity,
                                getString(R.string.create_entry_validation_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is CreateEntryResult.Failure -> {
                            Toast.makeText(
                                this@CreateEntryActivity,
                                result.message ?: getString(R.string.create_entry_failed),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } finally {
                    saveButton.isEnabled = true
                }
            }
        }
    }

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"

        fun newIntent(context: Context, userId: Long): Intent {
            return Intent(context, CreateEntryActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
        }
    }
}
