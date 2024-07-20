package com.example.dailynews.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dailynews.repository.NewsRepository

// NewsViewModelProviderFactory needs for instantiating ViewModel class. More precise - we define how our viewModel in our case NewsViewModel should be created

class NewsViewModelProviderFactory(val app: Application, val newsRepository: NewsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(app, newsRepository) as T
    }
}
