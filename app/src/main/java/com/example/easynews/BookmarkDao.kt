package com.example.easynews.`interface`

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.easynews.model.Bookmark

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmark")
    fun getAll(): List<Bookmark>

    @Query("SELECT * FROM bookmark WHERE url LIKE :url")
    fun findByUrl(url: String): Bookmark

    @Insert
    fun insertBookmark(bookmark: Bookmark)

    @Delete
    fun delete(bookmark: Bookmark)
}