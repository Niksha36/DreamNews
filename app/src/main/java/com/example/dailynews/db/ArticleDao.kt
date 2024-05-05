package com.example.dailynews.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dailynews.models.Article

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // this function will return inserted ids of items
    suspend fun upsert(article: Article): Long

    @Query("SELECT*FROM articleTable")
    fun getAllItems(): LiveData<List<Article>>

    @Delete
    suspend fun deleteItems(article: Article)
}