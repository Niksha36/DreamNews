package com.example.dailynews.ui.auth.auth_fragmens

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.dailynews.R
import com.example.dailynews.databinding.FragmentCreateAccountBinding
import com.example.dailynews.ui.auth.AuthActivity
import com.example.dailynews.ui.auth.AuthViewModel
import com.example.dailynews.util.EditTextUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CreateAccountFragment : Fragment(R.layout.fragment_create_account) {
    lateinit var binding: FragmentCreateAccountBinding
    lateinit var viewModel: AuthViewModel
    lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCreateAccountBinding.bind(view)
        viewModel = (activity as AuthActivity).viewModel
        val passwordIcon: Drawable? =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_lock)
        val repeatIcon: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_lock)
        val emaiIcon: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_email)
        EditTextUtils.setFocusChangeListener(
            requireContext(),
            binding.etCreateAccPasswort,
            passwordIcon
        )
        EditTextUtils.setFocusChangeListener(requireContext(), binding.etRepeatPassword, repeatIcon)
        EditTextUtils.setFocusChangeListener(
            requireContext(),
            binding.etCreateAccEmailAddress,
            emaiIcon
        )
        binding.tvSignin.setOnClickListener {
            findNavController().navigate(R.id.action_createAccountFragment_to_loginFragment)
        }
        auth = Firebase.auth
        binding.CreatingAccountButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val email = binding.etCreateAccEmailAddress.text.toString().trim()
        val password = binding.etCreateAccPasswort.text.toString().trim()
        val repeatPassword = binding.etRepeatPassword.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            // Show error message if email or password is empty
            Snackbar.make(
                binding.root,
                "Email and Password must not be empty",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        if ("@" !in email) {
            Snackbar.make(
                binding.root,
                "Invalid email format. Please check your email",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        if (password != repeatPassword) {
            Snackbar.make(binding.root, "Passwords do not match", Snackbar.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                viewModel.saveFirestoreState(false)
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "Registration successful", Snackbar.LENGTH_SHORT)
                        .show()
                    // Navigate to another fragment or activity
                    findNavController().navigate(R.id.action_createAccountFragment_to_emailVerification)
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthWeakPasswordException -> "Weak password. Please choose a stronger password."
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                    is FirebaseAuthUserCollisionException -> "This email is already in use."
                    else -> "Registration failed: ${e.message}"
                }
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
