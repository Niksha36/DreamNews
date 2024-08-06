package com.example.dailynews.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dailynews.R
import com.example.dailynews.databinding.ActivityNewsBinding
import com.example.dailynews.db.ArticleDatabase
import com.example.dailynews.repository.NewsRepository
import com.example.dailynews.util.AuthStates
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NewsActivity : AppCompatActivity() {
    lateinit var binding: ActivityNewsBinding
    lateinit var viewModel: NewsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newsRepository = NewsRepository(ArticleDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(application, newsRepository)

        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        if (intent.getBooleanExtra("CLEAR_DB", false)) {
            viewModel.clearDB()
        }
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val needsDataSync = sharedPreferences.getBoolean("needs_data_sync", false)

        if (needsDataSync) {
            viewModel.getFirestoreData()
            Log.e("Check needsDataSync", "needsDataSync = ${needsDataSync}")
            with(sharedPreferences.edit()) {
                putBoolean("needs_data_sync", false)
                apply()
            }
        } else {
            Log.e("Check needsDataSync", "needsDataSync = ${needsDataSync}")
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val bottomNavigationView = binding.bottomNavigationView
        val newsNavHostFragment = binding.newsNavHostFragment
        val navController = findNavController(R.id.newsNavHostFragment)
        // connecting bottom navigation with nav controller
        bottomNavigationView.setupWithNavController(newsNavHostFragment.findNavController())

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.breakingNewsFragment -> {
                    if (navController.currentDestination?.id != R.id.breakingNewsFragment) {
                        navController.navigate(R.id.breakingNewsFragment)
                    }
                    true
                }
                R.id.searchNewsFragment -> {
                    if (navController.currentDestination?.id != R.id.searchNewsFragment) {
                        navController.navigate(R.id.searchNewsFragment)
                    }
                    true
                }
                R.id.savedNewsFragment -> {
                    if (navController.currentDestination?.id != R.id.savedNewsFragment) {
                        navController.navigate(R.id.savedNewsFragment)
                    }
                    true
                }
                // Handle other cases for other menu items
                else -> false
            }
        }
    }

}