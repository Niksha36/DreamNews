package com.example.dailynews.ui.auth.auth_fragmens
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.dailynews.R
import com.example.dailynews.databinding.FragmentLoginBinding
import com.example.dailynews.ui.NewsActivity
import com.example.dailynews.ui.auth.AuthActivity
import com.example.dailynews.ui.auth.AuthViewModel
import com.example.dailynews.util.AuthStates
import com.example.dailynews.util.EditTextUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginFragment : Fragment(R.layout.fragment_login) {
    lateinit var binding: FragmentLoginBinding
    lateinit var viewModel: AuthViewModel
    lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        viewModel = (activity as AuthActivity).viewModel
        val lockIcon: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_lock)
        val emaiIcon: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_email)
        EditTextUtils.setFocusChangeListener(requireContext(), binding.etLoginPassword, lockIcon)
        EditTextUtils.setFocusChangeListener(
            requireContext(),
            binding.etLoginEmailAddress,
            emaiIcon
        )

        auth = Firebase.auth
        binding.sighInButton.setOnClickListener {
            signInUser()
        }
        binding.createAccountButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_createAccountFragment)
        }
        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_resetPasswordFragment)
        }
    }

    private fun signInUser() {
        val email = binding.etLoginEmailAddress.text.toString().trim()
        val password = binding.etLoginPassword.text.toString().trim()
        binding.progressBarLogin.visibility = View.VISIBLE
        if (email.isEmpty() || password.isEmpty()) {
            // Show error message if email or password is empty
            binding.progressBarLogin.visibility = View.INVISIBLE
            Snackbar.make(
                binding.root,
                "Email and Password must not be empty",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        if ("@" !in email) {
            binding.progressBarLogin.visibility = View.INVISIBLE
            Snackbar.make(
                binding.root,
                "Invalid email format. Please check your email",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                viewModel.saveAuthState(AuthStates.AUTHENTICATED)
                withContext(Dispatchers.Main) {
                    binding.progressBarLogin.visibility = View.INVISIBLE
                    Snackbar.make(binding.root, "Login successful", Snackbar.LENGTH_SHORT)
                        .show()
                    delay(1500)
                    viewModel.saveFirestoreState(true)
                    user = auth.currentUser!!
                    if (!user.isEmailVerified){
                        findNavController().navigate(R.id.action_loginFragment_to_emailVerification)
                    } else {
                        val intent = Intent(requireContext(), NewsActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra("CLEAR_DB", true)
                        }
                        startActivity(intent)
                    }

                }
            } catch (e: FirebaseException) {
                val message = when (e) {
                    is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> "Invalid email or password. Please try again."
                    else -> "Login failed. Please try again."
                }
                binding.progressBarLogin.visibility = View.INVISIBLE
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

}