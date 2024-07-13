package com.example.dailynews.repository

import com.example.dailynews.api.RetrofitInstance
import com.example.dailynews.db.ArticleDatabase

// Для чего нужен этот репозиторий???
class NewsRepository(
    val db: ArticleDatabase
) {
    // Получение всех новостей
    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        //getBreakingNews - это функция которая находится в NewsAPI
        RetrofitInstance.api?.getBreakingNews(countryCode, pageNumber)

    suspend fun searchNews(searchRequest: String, pageNumber: Int) =
        RetrofitInstance.api?.searchNews(searchRequest, pageNumber)
}