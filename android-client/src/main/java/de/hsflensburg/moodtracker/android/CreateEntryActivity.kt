package de.hsflensburg.moodtracker.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreateEntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_entry)

        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        if (userId <= 0L) {
            Toast.makeText(this, getString(R.string.entries_missing_user), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        findViewById<Button>(R.id.createEntrySave).setOnClickListener {
            Toast.makeText(this, getString(R.string.create_entry_placeholder), Toast.LENGTH_SHORT)
                .show()
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
