package com.example.easynews.`interface`

import com.example.easynews.model.News
import retrofit2.Call
import retrofit2.http.GET

import com.example.easynews.model.WebSite
import retrofit2.http.Url

interface NewsService {
    @get:
    GET("v2/sources?apiKey=848c158751374e079db06e2de9191ad5")
    val sources: Call<WebSite>

    @GET
    fun getNewsFromSource (@Url url: String): Call<News>
}