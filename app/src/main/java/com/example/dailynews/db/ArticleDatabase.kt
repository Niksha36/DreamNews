package com.example.dailynews.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dailynews.models.Article

@Database(
    entities = [Article::class],
    version = 6,
)
@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase() {
    abstract fun getArticleDao(): ArticleDao

    companion object {

        // Volatile annotation guarantee that in multi-threaded environment all the threads will see a latest version of instance variable
        // by making instance updates visible for other threads
        @Volatile
        private var instance: ArticleDatabase? = null
        private val LOCK = Any()
        private fun createDB(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ArticleDatabase::class.java,
                "articleDB.db"
            ).fallbackToDestructiveMigration().build()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDB(context).also { instance = it }
        }
    }
}