package com.example.dailynews.ui.auth.auth_fragmens
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dailynews.R
import com.example.dailynews.databinding.FragmentEmailVerificationBinding
import com.example.dailynews.ui.NewsActivity
import com.example.dailynews.ui.auth.AuthActivity
import com.example.dailynews.ui.auth.AuthViewModel
import com.example.dailynews.util.AuthStates
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EmailVerification : Fragment(R.layout.fragment_email_verification) {
    lateinit var binding:FragmentEmailVerificationBinding
    lateinit var user: FirebaseUser
    lateinit var viewModel: AuthViewModel
    val auth = FirebaseAuth.getInstance()
    private var checkVerificationJob: Job? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentEmailVerificationBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as AuthActivity).viewModel
        user = auth.currentUser!!
        binding.buttonGetVerificationLink.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    user.sendEmailVerification().await()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Verification email sent to ${user.email}", Toast.LENGTH_LONG).show()
                        Log.d("Auth", "Verification email sent to ${user.email}")
                    }
                    delay(3000)
                    startCheckingEmailVerification()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Failed to send verification email", Toast.LENGTH_LONG).show()
                        Log.e("Auth", "Failed to send verification email", e)
                    }
                }
            }
        }
    }
    private fun startCheckingEmailVerification() {
        checkVerificationJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                user.reload().await()
                if (user.isEmailVerified == true) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Email verified!", Toast.LENGTH_LONG).show()
                        Log.d("Auth", "Email verified for ${user.email}")
                        viewModel.saveAuthState(AuthStates.AUTHENTICATED)
                        val intent = Intent(requireContext(), NewsActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra("CLEAR_DB", true)
                        }
                        startActivity(intent)
                    }
                    break
                }
                delay(5000)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        checkVerificationJob?.cancel()
    }
}