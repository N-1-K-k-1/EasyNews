package com.example.easynews.common

import com.example.easynews.`interface`.NewsService
import com.example.easynews.remote.RetrofitClient

object Common {
    private const val BASE_URL = "https://newsapi.org/"

    val newsService: NewsService = RetrofitClient.getClient(BASE_URL).create(NewsService::class.java)

}