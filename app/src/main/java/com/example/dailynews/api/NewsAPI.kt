package com.example.dailynews.api

import com.example.dailynews.models.NewsResponse
import com.example.dailynews.util.Constants.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {

    @GET("v2/top-headlines")
    suspend fun breakingNews(
        @Query("country")
        countryName: String = "us",
        @Query("page")
        pageNumber:Int = 1,
        @Query("apiKey")
        api_key:String = API_KEY
    ): Response<NewsResponse>

    @GET("v2/everything")
    suspend fun searchNews(
        @Query("q")
        searchRequest: String,
        @Query("page")
        pageNumber:Int = 1,
        @Query("apiKey")
        api_key:String = API_KEY
    ): Response<NewsResponse>
}