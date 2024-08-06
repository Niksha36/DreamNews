package com.example.dailynews.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.dailynews.ui.auth.AuthActivity
import com.example.dailynews.ui.auth.AuthViewModel
import com.example.dailynews.util.AuthStates

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authState = viewModel.getAuthState()
        val intent = when(authState) {
            AuthStates.AUTHENTICATED -> Intent(this, NewsActivity::class.java)
            AuthStates.NOT_AUTHENTICATED -> Intent(this, AuthActivity::class.java)
            AuthStates.SIGN_IN_LATER -> Intent(this, NewsActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}