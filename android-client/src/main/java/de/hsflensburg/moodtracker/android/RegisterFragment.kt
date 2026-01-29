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
import model.RegisterInput
import model.RegisterModel
import model.RegisterResult
import model.RegisterValidation

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private val client = MoodTrackerClientProvider.client
    private val registerModel by lazy { RegisterModel(client) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameLayout = view.findViewById<TextInputLayout>(R.id.registerUsernameLayout)
        val emailLayout = view.findViewById<TextInputLayout>(R.id.registerEmailLayout)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.registerPasswordLayout)

        val usernameInput = view.findViewById<TextInputEditText>(R.id.registerUsername)
        val emailInput = view.findViewById<TextInputEditText>(R.id.registerEmail)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.registerPassword)

        val registerButton = view.findViewById<MaterialButton>(R.id.registerButton)
        val loginButton = view.findViewById<View>(R.id.registerLoginButton)

        val errorText = view.findViewById<TextView>(R.id.registerErrorText)

        fun clearFieldErrors() {
            usernameLayout.error = null
            emailLayout.error = null
            passwordLayout.error = null
        }

        fun showGeneralError(message: String?) {
            errorText.text = message ?: getString(R.string.register_failed)
            errorText.visibility = View.VISIBLE
        }

        fun clearGeneralError() {
            errorText.text = ""
            errorText.visibility = View.GONE
        }

        fun applyValidation(validation: RegisterValidation) {
            usernameLayout.error = when {
                validation.missingUsername -> getString(R.string.error_required_field)
                validation.invalidUsername -> getString(R.string.error_username_length)
                else -> null
            }

            emailLayout.error = when {
                validation.missingEmail -> getString(R.string.error_required_field)
                validation.invalidEmail -> getString(R.string.error_invalid_email)
                else -> null
            }

            passwordLayout.error = when {
                validation.missingPassword -> getString(R.string.error_required_field)
                validation.invalidPassword -> getString(R.string.error_password_length)
                else -> null
            }
        }

        usernameInput.addTextChangedListener {
            usernameLayout.error = null
            clearGeneralError()
        }
        emailInput.addTextChangedListener {
            emailLayout.error = null
            clearGeneralError()
        }
        passwordInput.addTextChangedListener {
            passwordLayout.error = null
            clearGeneralError()
        }

        registerButton.setOnClickListener {
            if (!registerButton.isEnabled) return@setOnClickListener

            clearFieldErrors()
            clearGeneralError()

            val input = RegisterInput(
                username = usernameInput.text?.toString().orEmpty(),
                email = emailInput.text?.toString().orEmpty(),
                password = passwordInput.text?.toString().orEmpty()
            )

            registerButton.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    when (val result = registerModel.register(input)) {
                        is RegisterResult.ValidationError -> {
                            applyValidation(result.validation)
                        }

                        is RegisterResult.Success -> {
                            if (!isAdded) return@launch

                            Toast.makeText(
                                requireContext(),
                                getString(R.string.register_success),
                                Toast.LENGTH_LONG
                            ).show()

                            val intent = Intent(requireContext(), EntriesActivity::class.java).apply {
                                putExtra(
                                    EntriesActivity.EXTRA_USER_ID,
                                    result.loginResponse.userId
                                )
                            }
                            startActivity(intent)
                            activity?.finish()
                        }

                        is RegisterResult.Failure -> {
                            if (!isAdded) return@launch
                            showGeneralError(
                                result.message ?: getString(R.string.register_failed)
                            )
                        }
                    }
                } finally {
                    registerButton.isEnabled = true
                }
            }
        }

        loginButton.setOnClickListener {
            activity?.findViewById<ViewPager2>(R.id.authPager)?.currentItem = 0
        }
    }
}
