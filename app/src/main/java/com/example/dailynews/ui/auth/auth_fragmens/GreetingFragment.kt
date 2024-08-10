package com.example.dailynews.ui.auth.auth_fragmens
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.dailynews.R
import com.example.dailynews.databinding.FragmentGreetingBinding
import com.example.dailynews.ui.NewsActivity
import com.example.dailynews.ui.auth.AuthActivity
import com.example.dailynews.ui.auth.AuthViewModel
import com.example.dailynews.util.AuthStates


class GreetingFragment : Fragment(R.layout.fragment_greeting) {
    lateinit var binding:FragmentGreetingBinding
    lateinit var viewModel: AuthViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGreetingBinding.bind(view)
        viewModel = (activity as AuthActivity).viewModel

        binding.buttonWithoutAcc.setOnClickListener {
            viewModel.exitAccount()
            val intent = Intent(requireContext(), NewsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (viewModel.getAuthState().name != AuthStates.SIGN_IN_LATER.name) {
                    putExtra("CLEAR_DB", true)
                }
            }
            viewModel.saveAuthState(AuthStates.SIGN_IN_LATER)
            startActivity(intent)
        }
        binding.GreetingSignInButton.setOnClickListener {
            findNavController().navigate(R.id.action_greetingFragment_to_loginFragment)
        }
    }
}