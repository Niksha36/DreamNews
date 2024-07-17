package com.example.dailynews.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailynews.models.Article
import com.example.dailynews.models.NewsResponse
import com.example.dailynews.repository.NewsRepository
import com.example.dailynews.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(
    // Что такое newsRepository? Это класс который будет предоставялть нам дрступ к бд в ViewModel
    // We can not implement parameters in ViewModel without ViewModelProviderFactory.
    val newsRepository: NewsRepository
) : ViewModel() {
    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponses:NewsResponse ?= null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponses:NewsResponse ?= null
    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(countryName: String) = viewModelScope.launch {
        breakingNews.postValue(Resource.Loading())
        val response = newsRepository.getBreakingNews(countryName, breakingNewsPage)
        breakingNews.postValue(response?.let { handleBreakingNewsResponse(it) })
    }

    fun getSearchingNews(searchRequest: String) = viewModelScope.launch {
        searchNewsPage = 1
        searchNewsResponses = null
        searchNews.postValue(Resource.Loading())
        val response = newsRepository.searchNews(searchRequest, searchNewsPage)
        searchNews.postValue(response?.let { handleSearchNewsResponse(it) })
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage ++
                if (breakingNewsResponses == null) {
                    breakingNewsResponses = resultResponse
                } else {
                    val oldList = breakingNewsResponses?.articles
                    val newList= resultResponse.articles
                    // this line is equivalent to breakingNewsResponses?.articles.addAll(resultResponse.articles)
                    oldList?.addAll(newList)
                }
                return Resource.Success(breakingNewsResponses ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage ++
                if (searchNewsResponses == null){
                    searchNewsResponses = resultResponse
                } else{
                    val oldList = searchNewsResponses?.articles
                    val newList = resultResponse.articles
                    oldList?.addAll(newList)
                }
                return Resource.Success(searchNewsResponses ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    // Добавлю функции из NewsResponse которые взаиможействуют с DAO
    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }
    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }
    fun getSavedArticles() = newsRepository.getAllArticles()
}