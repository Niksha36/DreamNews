package com.example.dailynews.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dailynews.repository.NewsRepository

// NewsViewModelProviderFactory needs for instantiating ViewModel class. More precise - we define how our viewModel in our case NewsViewModel should be created

class NewsViewModelProviderFactory(val newsRepository: NewsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRepository) as T
    }
}
