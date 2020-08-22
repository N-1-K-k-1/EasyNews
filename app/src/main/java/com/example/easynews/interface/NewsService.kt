package com.example.easynews.`interface`

import com.example.easynews.model.News
import retrofit2.Call
import retrofit2.http.*


interface NewsService {

    @Headers("X-Api-Key: 848c158751374e079db06e2de9191ad5")
    @GET("v2/everything?sortBy=publishedAt&pageSize=100")
    fun getLocalNews(@Query("q") cityName: String, @Query("language") lang: String) : Call<News>

    @GET
    fun getNewsFromSource (@Url url: String): Call<News>
}