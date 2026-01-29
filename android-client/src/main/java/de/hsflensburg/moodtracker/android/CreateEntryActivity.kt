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

        val titleInput = findViewById<EditText>(R.id.createEntryName)
        val contentInput = findViewById<EditText>(R.id.createEntryContent)
        val moodInput = findViewById<EditText>(R.id.createEntryMood)
        val saveButton = findViewById<Button>(R.id.createEntrySave)
        val createEntryModel = CreateEntryModel(client)

        saveButton.setOnClickListener {
            val input = CreateEntryInput(
                title = titleInput.text.toString(),
                content = contentInput.text.toString(),
                moodRatingInput = moodInput.text.toString()
            )
            saveButton.isEnabled = false
            lifecycleScope.launch {
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
                        val message = createEntryModel.validationMessage(result.validation)
                            ?: getString(R.string.create_entry_validation_failed)
                        Toast.makeText(this@CreateEntryActivity, message, Toast.LENGTH_LONG).show()
                    }
                    is CreateEntryResult.Failure -> {
                        Toast.makeText(
                            this@CreateEntryActivity,
                            result.message ?: getString(R.string.create_entry_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                saveButton.isEnabled = true
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
