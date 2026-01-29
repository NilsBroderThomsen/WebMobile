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
import model.RegisterInput
import model.RegisterModel
import model.RegisterResult

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private val client = MoodTrackerClientProvider.client

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registerModel = RegisterModel(client)
        val usernameLayout = view.findViewById<TextInputLayout>(R.id.registerUsernameLayout)
        val emailLayout = view.findViewById<TextInputLayout>(R.id.registerEmailLayout)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.registerPasswordLayout)
        val usernameInput = view.findViewById<TextInputEditText>(R.id.registerUsername)
        val emailInput = view.findViewById<TextInputEditText>(R.id.registerEmail)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.registerPassword)
        val registerButton = view.findViewById<MaterialButton>(R.id.registerButton)

        registerButton.setOnClickListener {
            usernameLayout.error = null
            emailLayout.error = null
            passwordLayout.error = null

            val input = RegisterInput(
                username = usernameInput.text?.toString().orEmpty(),
                email = emailInput.text?.toString().orEmpty(),
                password = passwordInput.text?.toString().orEmpty()
            )

            val validation = registerModel.validate(input)
            if (validation.hasErrors) {
                if (validation.missingUsername) {
                    usernameLayout.error = getString(R.string.error_required_field)
                }
                if (validation.missingEmail) {
                    emailLayout.error = getString(R.string.error_required_field)
                }
                if (validation.missingPassword) {
                    passwordLayout.error = getString(R.string.error_required_field)
                }
                return@setOnClickListener
            }

            registerButton.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                when (val result = registerModel.register(input)) {
                    is RegisterResult.ValidationError -> {
                        if (result.validation.missingUsername) {
                            usernameLayout.error = getString(R.string.error_required_field)
                        }
                        if (result.validation.missingEmail) {
                            emailLayout.error = getString(R.string.error_required_field)
                        }
                        if (result.validation.missingPassword) {
                            passwordLayout.error = getString(R.string.error_required_field)
                        }
                    }
                    is RegisterResult.Success -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.register_success),
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = Intent(requireContext(), EntriesActivity::class.java).apply {
                            putExtra(EntriesActivity.EXTRA_USER_ID, result.loginResponse.userId)
                        }
                        startActivity(intent)
                        activity?.finish()
                    }
                    is RegisterResult.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            result.message ?: getString(R.string.register_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                registerButton.isEnabled = true
            }
        }

        view.findViewById<View>(R.id.registerLoginButton).setOnClickListener {
            activity?.findViewById<ViewPager2>(R.id.authPager)?.currentItem = 0
        }
    }
}
