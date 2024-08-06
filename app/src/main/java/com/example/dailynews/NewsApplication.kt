package com.example.dailynews

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class NewsApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}