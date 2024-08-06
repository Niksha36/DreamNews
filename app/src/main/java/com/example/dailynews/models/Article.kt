package com.example.dailynews.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "articleTable")
data class Article(
    @PrimaryKey
    val url: String = "",
    val author: String? = null,
    val content: String? = null,
    val description: String? = null,
    val publishedAt: String? = null,
    val source: Source? = null,
    val title: String? = null,
    val urlToImage: String? = null
) : Serializable