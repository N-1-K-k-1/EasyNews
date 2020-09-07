package com.example.easynews.common

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.easynews.BookmarkDao
import com.example.easynews.model.Bookmark

@Database(entities = [Bookmark::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration().build()
            }

            return instance!!
        }

        fun destroyDB() {
            instance = null
        }

        @Volatile
        private var instance: AppDatabase? = null
        private const val DB_NAME = "bookmarks"
    }
}