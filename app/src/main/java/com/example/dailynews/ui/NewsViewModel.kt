package com.example.dailynews.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dailynews.NewsApplication
import com.example.dailynews.models.Article
import com.example.dailynews.models.NewsResponse
import com.example.dailynews.repository.NewsRepository
import com.example.dailynews.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okio.IOException
import retrofit2.Response
class NewsViewModel(
    //newsRepository - это класс который будет предоставялть нам дрступ к бд в ViewModel
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
    // Firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    val email = currentUser?.email
    private val userId: String? = currentUser?.uid
    val firestoreDb = Firebase.firestore
    //saving date to firestore
    fun saveToFireStore(article: Article) = viewModelScope.launch(Dispatchers.IO) {
        try {
            userId?.let {
                val documentPath = "users/$it/articles"
                firestoreDb.collection(documentPath).add(article).await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error saving article to Firestore", e)
        }
    }
    //deleting data from firestore
    fun deleteFromFireStore(article: Article) = viewModelScope.launch(Dispatchers.IO) {
        userId?.let { uid ->
            try {
                val documentPath = "users/$uid/articles"
                val articleToDelete = firestoreDb.collection(documentPath)
                    .whereEqualTo("url", article.url)
                    .get()
                    .await()

                articleToDelete.documents.forEach {
                    firestoreDb.collection(documentPath).document(it.id).delete().await()
                }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error deleting article from Firestore", e)
            }
        }
    }
    //Retrieving Data from firestore
    fun getFirestoreData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            userId?.let { uid ->
                val documentPath = "users/$uid/articles"
                // Retrieve articles from Firestore
                val articlesSnapshot = firestoreDb.collection(documentPath).get().await()
                val articles = articlesSnapshot.toObjects(Article::class.java)

                // Save each article to the local database
                articles.forEach { article ->
                    newsRepository.upsert(article)
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error getting Firestore data", e)
        }
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

    fun clearDB() = viewModelScope.launch(Dispatchers.IO) {
        newsRepository.delAllArticles()
    }

}

