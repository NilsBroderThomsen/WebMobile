package de.hsflensburg.moodtracker.android

import android.content.Context
import android.content.res.Configuration
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate

object ThemeToggleHelper {
    fun toggle(context: Context) {
        val enableDarkMode = !isNightModeEnabled(context)
        AppCompatDelegate.setDefaultNightMode(
            if (enableDarkMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    fun updateButtonText(button: Button, context: Context) {
        button.setText(
            if (isNightModeEnabled(context)) {
                R.string.action_light_mode
            } else {
                R.string.action_dark_mode
            }
        )
    }

    private fun isNightModeEnabled(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
}
