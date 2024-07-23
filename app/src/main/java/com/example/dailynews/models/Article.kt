package com.example.dailynews.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "articleTable")
data class Article(
    @PrimaryKey
    val url: String,
    val author: String?,
    val content: String?,
    val description: String?,
    val publishedAt: String?,
    val source: Source?,
    val title: String?,
    val urlToImage: String?
) : Serializable