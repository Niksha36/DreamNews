package com.example.dailynews.ui.auth.auth_fragmens
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dailynews.R
import com.example.dailynews.databinding.FragmentEmailVerificationBinding
import com.example.dailynews.ui.NewsActivity
import com.example.dailynews.ui.auth.AuthActivity
import com.example.dailynews.ui.auth.AuthViewModel
import com.example.dailynews.util.AuthStates
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.handleCoroutineException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

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
            if (checkVerificationJob?.isActive == true) {
                Toast.makeText(requireContext(), "You can try again in 5 minutes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    user.sendEmailVerification().await()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Verification email sent to ${user.email}", Toast.LENGTH_LONG).show()
                        Log.d("Auth", "Verification email sent to ${user.email}")
                    }
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
    val handler = CoroutineExceptionHandler{_, throwable ->
        Toast.makeText(requireContext(), "Verification process timed out", Toast.LENGTH_LONG).show()
        Log.d("Auth", "Verification process timed out. $throwable")
    }
    private fun startCheckingEmailVerification() {
        checkVerificationJob = CoroutineScope(Dispatchers.IO+handler).launch {
            withTimeout(5 * 60 * 1000) {
                while (isActive) {
                    user.reload().await()
                    if (user.isEmailVerified == true) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Email verified!", Toast.LENGTH_LONG)
                                .show()
                            Log.d("Auth", "Email verified for ${user.email}")
                            viewModel.saveAuthState(AuthStates.AUTHENTICATED)
                            val intent = Intent(requireContext(), NewsActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
    }
    override fun onDestroyView() {
        super.onDestroyView()
        checkVerificationJob?.cancel()
    }
}