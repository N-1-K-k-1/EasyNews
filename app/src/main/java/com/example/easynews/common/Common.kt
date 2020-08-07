package com.example.easynews.common

import com.example.easynews.`interface`.NewsService
import com.example.easynews.remote.RetrofitClient

object Common {
    val BASE_URL = "https://newsapi.org/"
    val API_KEY = "848c158751374e079db06e2de9191ad5"

    val newsService: NewsService
        get() = RetrofitClient.getClient(BASE_URL).create(NewsService::class.java)

    fun getNewsAPI(source: String): String {
        return StringBuilder("https://newsapi.org/v2/top-headlines?sources=")
            .append(source)
            .append(API_KEY)
            .toString()
    }
}