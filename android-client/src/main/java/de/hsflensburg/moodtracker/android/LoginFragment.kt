package de.hsflensburg.moodtracker.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import model.LoginInput
import model.LoginModel
import model.LoginResult

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val client = MoodTrackerClientProvider.client
    private val loginModel = LoginModel(client)

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

            loginButton.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                when (
                    val result = loginModel.login(
                        LoginInput(
                            username = usernameInput.text?.toString().orEmpty(),
                            password = passwordInput.text?.toString().orEmpty()
                        )
                    )
                ) {
                    is LoginResult.ValidationError -> {
                        if (result.validation.missingUsername) {
                            usernameLayout.error = getString(R.string.error_required_field)
                        }
                        if (result.validation.missingPassword) {
                            passwordLayout.error = getString(R.string.error_required_field)
                        }
                    }
                    is LoginResult.Success -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.login_success),
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = Intent(requireContext(), EntriesActivity::class.java).apply {
                            putExtra(EntriesActivity.EXTRA_USER_ID, result.loginResponse.userId)
                        }
                        startActivity(intent)
                        activity?.finish()
                    }
                    is LoginResult.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            result.message ?: getString(R.string.login_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                loginButton.isEnabled = true
            }
        }

        view.findViewById<View>(R.id.loginRegisterButton).setOnClickListener {
            activity?.findViewById<ViewPager2>(R.id.authPager)?.currentItem = 1
        }
    }
}
