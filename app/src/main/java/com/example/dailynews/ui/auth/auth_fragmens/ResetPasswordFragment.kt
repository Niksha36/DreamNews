package com.example.dailynews.ui.auth.auth_fragmens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.dailynews.R
import com.example.dailynews.databinding.FragmentResetPasswordBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {
    lateinit var binding:FragmentResetPasswordBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentResetPasswordBinding.bind(view)

        binding.buttonResetPassword.setOnClickListener {
            val email = binding.etEmailToResetPassword.text.toString().trim()
            binding.progressBar.visibility = View.VISIBLE
            if (email.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter your email address",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility = View.INVISIBLE
                return@setOnClickListener
            }
            if (!email.contains("@")) {
                Toast.makeText(
                    requireContext(),
                    "Invalid email format. Please check your email",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility = View.INVISIBLE
                return@setOnClickListener
            }


            Firebase.auth.sendPasswordResetEmail(email).addOnCompleteListener{task->
                if (task.isSuccessful){
                    Toast.makeText(requireContext(), "Link was successfully send to your email", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.INVISIBLE
                    findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment)
                } else {
                    Toast.makeText(requireContext(), task.exception?.toString(), Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }
}