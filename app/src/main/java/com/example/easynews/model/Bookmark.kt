package com.example.easynews.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmark")
data class Bookmark(
    @PrimaryKey
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "title")
    val title: String? = null,
    @ColumnInfo(name = "published_at")
    val publishedAt: String? = null,
    @ColumnInfo(name = "image")
    val image: String? = null
)