package de.hsflensburg.moodtracker.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
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
import model.LoginValidation

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val client = MoodTrackerClientProvider.client
    private val loginModel by lazy { LoginModel(client) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameLayout = view.findViewById<TextInputLayout>(R.id.loginUsernameLayout)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.loginPasswordLayout)

        val usernameInput = view.findViewById<TextInputEditText>(R.id.loginUsername)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.loginPassword)

        val loginButton = view.findViewById<MaterialButton>(R.id.loginButton)
        val registerButton = view.findViewById<View>(R.id.loginRegisterButton)

        val errorText = view.findViewById<TextView>(R.id.loginErrorText)

        fun clearFieldErrors() {
            usernameLayout.error = null
            passwordLayout.error = null
        }

        fun showGeneralError(message: String?) {
            errorText.text = message ?: getString(R.string.login_failed)
            errorText.visibility = View.VISIBLE
        }

        fun clearGeneralError() {
            errorText.text = ""
            errorText.visibility = View.GONE
        }

        fun applyValidation(validation: LoginValidation) {
            usernameLayout.error = when {
                validation.missingUsername -> getString(R.string.error_required_field)
                else -> null
            }
            passwordLayout.error = when {
                validation.missingPassword -> getString(R.string.error_required_field)
                else -> null
            }
        }

        usernameInput.addTextChangedListener {
            usernameLayout.error = null
            clearGeneralError()
        }
        passwordInput.addTextChangedListener {
            passwordLayout.error = null
            clearGeneralError()
        }

        loginButton.setOnClickListener {
            if (!loginButton.isEnabled) return@setOnClickListener

            clearFieldErrors()
            clearGeneralError()

            val input = LoginInput(
                username = usernameInput.text?.toString().orEmpty(),
                password = passwordInput.text?.toString().orEmpty()
            )

            loginButton.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    when (val result = loginModel.login(input)) {
                        is LoginResult.ValidationError -> {
                            applyValidation(result.validation)
                        }

                        is LoginResult.Success -> {
                            if (!isAdded) return@launch

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
                            if (!isAdded) return@launch
                            showGeneralError(result.message ?: getString(R.string.login_failed))
                        }
                    }
                } finally {
                    loginButton.isEnabled = true
                }
            }
        }

        registerButton.setOnClickListener {
            activity?.findViewById<ViewPager2>(R.id.authPager)?.currentItem = 1
        }
    }
}