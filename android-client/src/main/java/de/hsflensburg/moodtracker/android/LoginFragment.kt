package de.hsflensburg.moodtracker.android

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import api.MoodTrackerClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val client = MoodTrackerClient(AppConfig.BASE_URL)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameLayout = view.findViewById<TextInputLayout>(R.id.loginUsernameLayout)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.loginPasswordLayout)
        val usernameInput = view.findViewById<TextInputEditText>(R.id.loginUsername)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.loginPassword)
        val loginButton = view.findViewById<MaterialButton>(R.id.loginButton)

        loginButton.setOnClickListener {
            usernameLayout.error = null
            passwordLayout.error = null

            val username = usernameInput.text?.toString()?.trim().orEmpty()
            val password = passwordInput.text?.toString()?.trim().orEmpty()

            var hasError = false
            if (username.isBlank()) {
                usernameLayout.error = getString(R.string.error_required_field)
                hasError = true
            }
            if (password.isBlank()) {
                passwordLayout.error = getString(R.string.error_required_field)
                hasError = true
            }
            if (hasError) {
                return@setOnClickListener
            }

            loginButton.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    client.login(username = username, password = password)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.login_success),
                        Toast.LENGTH_LONG
                    ).show()
                } catch (ex: Exception) {
                    Toast.makeText(
                        requireContext(),
                        ex.message ?: getString(R.string.login_failed),
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    loginButton.isEnabled = true
                }
            }
        }

        view.findViewById<View>(R.id.loginRegisterButton).setOnClickListener {
            activity?.findViewById<ViewPager2>(R.id.authPager)?.currentItem = 1
        }
    }
}
