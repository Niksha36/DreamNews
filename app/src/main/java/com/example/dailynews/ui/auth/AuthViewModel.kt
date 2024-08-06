package com.example.dailynews.ui.auth

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailynews.util.AuthStates
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel(app:Application): AndroidViewModel(app) {
    private val sharedPreferences = app.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    fun saveAuthState(state: AuthStates) {
        with(sharedPreferences.edit()) {
            putString("auth_state", state.name)
            apply()
        }
    }

    fun saveFirestoreState(state: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("needs_data_sync", state)
            apply()
        }
    }

    fun getAuthState():AuthStates {
        val stateName = sharedPreferences.getString("auth_state", null)
        return AuthStates.valueOf(stateName ?: AuthStates.NOT_AUTHENTICATED.name)
    }

    fun exitAccount() = viewModelScope.launch(Dispatchers.IO) {
        try {
            auth.signOut()
        } catch(e: FirebaseException){
            Log.e("FirestoreError", "Error getting Firestore data", e)
        }
    }
}