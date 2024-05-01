package com.example.dailynews.ui

data class NewsResponse(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)