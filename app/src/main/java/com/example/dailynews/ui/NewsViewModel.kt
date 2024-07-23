package com.example.dailynews.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Button
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dailynews.NewsApplication
import com.example.dailynews.models.Article
import com.example.dailynews.models.NewsResponse
import com.example.dailynews.repository.NewsRepository
import com.example.dailynews.util.Resource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response
class NewsViewModel(
    // Что такое newsRepository? Это класс который будет предоставялть нам дрступ к бд в ViewModel
    // We can not implement parameters in ViewModel without ViewModelProviderFactory.
    app:Application,
    val newsRepository: NewsRepository
) : AndroidViewModel(app) {
    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponses:NewsResponse ?= null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponses:NewsResponse ?= null

    val category:MutableLiveData<String> = MutableLiveData("")
    val activeButtonId: MutableLiveData<Int?> = MutableLiveData(null)

    init {
        getBreakingNews("us", "")
    }

    fun getBreakingNews(countryName: String, sortCategory:String) = viewModelScope.launch {
        safeBreakingNewsCall(countryName, sortCategory)
    }

    fun getSearchingNews(searchRequest: String) = viewModelScope.launch {
        if (searchRequest.isBlank()) {
            searchNews.postValue(Resource.Success(NewsResponse(mutableListOf(), "ok", 0)))
            return@launch
        }
        searchNewsPage = 1
        searchNewsResponses = null
        safeSearchingNewsCall(searchRequest)
    }

    private suspend fun safeBreakingNewsCall(countryName: String, sortCategory: String) {
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryName, breakingNewsPage, sortCategory)
                breakingNews.postValue(response?.let { handleBreakingNewsResponse(it) })
            } else {
                breakingNews.postValue(Resource.Error("Please check your internet connection and try again"))
            }
        }
        catch (t: Throwable) {
            when(t) {
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun safeSearchingNewsCall(searchRequest: String) {
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.searchNews(searchRequest, searchNewsPage)
                searchNews.postValue(response?.let { handleSearchNewsResponse(it) })
            } else {
                searchNews.postValue(Resource.Error("Please check your internet connection and try again"))
            }
        }
        catch (t: Throwable) {
            when(t) {
                is IOException -> searchNews.postValue(Resource.Error("Network Failure"))
                else -> searchNews.postValue(Resource.Error("Conversion Error"))
            }
        }
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

    fun isArticleInDb(url:String): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            result.postValue(newsRepository.getArticleByUrl(url)!=null)
        }
        return result
    }
    // checking internet connection function
    private fun hasInternetConnection(): Boolean{
        val connectivityManager = getApplication<NewsApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }

    }

}

