package com.example.dailynews.repository

import com.example.dailynews.api.RetrofitInstance
import com.example.dailynews.db.ArticleDatabase
import com.example.dailynews.models.Article

class NewsRepository(
    val db: ArticleDatabase
) {
    // Получение всех новостей
    suspend fun getBreakingNews(countryCode: String, pageNumber: Int, sortCategory:String) =
        //getBreakingNews - это функция которая находится в NewsAPI
        RetrofitInstance.api?.getBreakingNews(countryCode, pageNumber, sortCategory)

    suspend fun searchNews(searchRequest: String, pageNumber: Int) =
        RetrofitInstance.api?.searchNews(searchRequest, pageNumber)

    // Добавим функции из DAO
    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)
    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteItems(article)
    fun getAllArticles() = db.getArticleDao().getAllItems()
    suspend fun getArticleByUrl(url:String) = db.getArticleDao().getArticleByUrl(url)

}