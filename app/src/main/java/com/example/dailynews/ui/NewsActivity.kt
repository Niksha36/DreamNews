package com.example.dailynews.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dailynews.R
import com.example.dailynews.databinding.ActivityNewsBinding

class NewsActivity : AppCompatActivity() {
    lateinit var binding: ActivityNewsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val bottomNavigationView = binding.bottomNavigationView
        val newsNavHostFragment = binding.newsNavHostFragment
        // connecting bottom navigation with nav controller
        bottomNavigationView.setupWithNavController(newsNavHostFragment.findNavController())
    }

}