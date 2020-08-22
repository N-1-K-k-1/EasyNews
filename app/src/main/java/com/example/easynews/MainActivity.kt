package com.example.easynews

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easynews.`interface`.NewsService
import com.example.easynews.adapter.viewHolder.ListNewsAdapter
import com.example.easynews.common.Common
import com.example.easynews.common.GeoData
import com.example.easynews.common.NoImage
import com.example.easynews.model.News
import com.example.easynews.network.CheckNetwork
import com.example.easynews.network.GlobalVars
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.lang.Exception
import java.net.URL


class MainActivity : AppCompatActivity() {

    var topUrl: String = ""
    var exceptions: Array<String>? = null

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var mService: NewsService
    lateinit var adapter: ListNewsAdapter
    lateinit var dialog: AlertDialog
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var toolbar: androidx.appcompat.app.ActionBar
    private lateinit var geoData: GeoData

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(AppCompatDelegate.FEATURE_SUPPORT_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)

        /* Detect location and check permissions */
        geoData = GeoData(this, savedInstanceState)
        geoData.checkLocation()

        /* Check connection */
        val network = CheckNetwork(this)
        // Register NetworkCallback
        network.registerNetworkCallback()

        /* Cache */
        Paper.init(this)

        /* Service */
        mService = Common.newsService

        setContentView(R.layout.activity_main)

        /* Bottom navigation */
        bottomNav = findViewById(R.id.navigationView)
        bottomNav.setOnNavigationItemSelectedListener(navListener)

        /* Top action bar */
        toolbar = supportActionBar!!
    }

    override fun onStart() {
        super.onStart()
        if (geoData.getCityName() != "")
            toolbar.title = getString(R.string.local_news_for) + " " + geoData.getCityName()

        toolbar.isHideOnContentScrollEnabled = true

        /* View */
        swipe_to_refresh.setOnRefreshListener { loadWebSiteSource(true) }

        diagonalLayout.setOnClickListener {
            val detail = Intent(baseContext, NewsDetails::class.java)
            detail.putExtra("webUrl", topUrl)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                detail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(detail)
        }

        list_news.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        list_news.layoutManager = layoutManager

        dialog = SpotsDialog.Builder().setContext(this).build()

        loadWebSiteSource(false)

    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener {item ->
        when (item.itemId) {
            R.id.navigation_local -> {
                if (geoData.getCityName() != "")
                    toolbar.title = getString(R.string.local_news_for) + " " + geoData.getCityName()
                else
                    toolbar.title = getString(R.string.local_news)
                return@OnNavigationItemSelectedListener  true
            }
            R.id.navigation_global -> {
                toolbar.title = getString(R.string.global_news)
                val globalIntent = Intent(baseContext, ListNews::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    globalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(globalIntent)
                return@OnNavigationItemSelectedListener true
            }
            else -> {
                false
            }
        }
    }

    private fun loadWebSiteSource(isRefreshed: Boolean) {

        // If not refreshed, then read cache, else load website and write cache
        if (!isRefreshed) {

            GlobalVars.counter++

            val cache = Paper.book().read<String>("cache")

            if (cache != null && !cache.isBlank() && cache != "null" && !GlobalVars.isNetworkConnected)
            {
                val news = Gson().fromJson<News>(cache, News::class.java)

                Picasso.get().load(news.articles?.get(0)?.urlToImage).into(top_image)
                top_title.text = news.articles?.get(0)?.title
                top_author.text = news.articles?.get(0)?.author

                val firstItemRemoved = news.articles
                firstItemRemoved?.removeAt(0)

                adapter = firstItemRemoved?.let { ListNewsAdapter(baseContext, it) }!!
                adapter.notifyDataSetChanged()
                list_news.adapter = adapter
            }
            else
            {
                geoData.getCityName()?.let {
                    mService.getLocalNews("$it OR ${it}е", geoData.getLanguage()).enqueue(
                        object : retrofit2.Callback<News> {
                            var i = 0
                            override fun onResponse(call: Call<News>, response: Response<News>) {
                                Paper.book().write("cache", Gson().toJson(response.body()!!))
                                response.body()!!.articles?.forEach { it ->
                                    try {
                                        it.urlToImage?.let { it1 -> Log.e("Counter", it1) }
                                        URL(it.urlToImage).readBytes()
                                    } catch (e: Exception) {
                                        println(e)
                                        i++
                                        it.isImage = false
                                    }
                                }
                                Log.e("Counter", i.toString())

                                Picasso.get()
                                    .load(response.body()?.articles?.get(0)?.urlToImage)
                                    .into(top_image)

                                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                                StrictMode.setThreadPolicy(policy)

                                try {
                                    URL(response.body()?.articles?.get(0)?.urlToImage).readBytes()
                                } catch (e: IOException) {
                                    Picasso.get().load(R.drawable.no_image_available).into(top_image)
                                }

                                top_title.text = response.body()!!.articles!![0].title
                                top_author.text = response.body()!!.articles!![0].author
                                topUrl = response.body()!!.articles!![0].url.toString()

                                // Load all articles that remains
                                val firstItemRemoved = response.body()!!.articles
                                firstItemRemoved!!.removeAt(0)

                                adapter = ListNewsAdapter(baseContext, firstItemRemoved)
                                adapter.notifyDataSetChanged()
                                list_news.adapter = adapter

                                dialog.dismiss()
                            }

                            override fun onFailure(call: Call<News>, t: Throwable) {
                                Toast.makeText(baseContext, "Failed to load", Toast.LENGTH_SHORT).show()

                                dialog.dismiss()
                            }
                        }
                    )
                }
            }
        }
        else
        {
            swipe_to_refresh.isRefreshing = true

            NoImage.data?.clear()

            // Fetch new data
            geoData.getCityName()?.let {
                mService.getLocalNews("$it OR ${it}е", geoData.getLanguage()).enqueue(
                    object : retrofit2.Callback<News> {
                        override fun onResponse(call: Call<News>, response: Response<News>) {
                            Paper.book().write("cache", Gson().toJson(response.body()!!))

                            Picasso.get()
                                .load(response.body()?.articles?.get(0)?.urlToImage)
                                .into(top_image)

                            try {
                                URL(response.body()?.articles?.get(0)?.urlToImage).readBytes()
                            } catch (e: IOException) {
                                Picasso.get().load(R.drawable.no_image_available).into(top_image)
                            }

                            top_title.text = response.body()!!.articles!![0].title
                            top_author.text = response.body()!!.articles!![0].author
                            topUrl = response.body()!!.articles!![0].url.toString()

                            val firstItemRemoved = response.body()!!.articles
                            firstItemRemoved!!.removeAt(0)

                            adapter = ListNewsAdapter(baseContext, firstItemRemoved)
                            adapter.notifyDataSetChanged()
                            list_news.adapter = adapter

                            swipe_to_refresh.isRefreshing = false
                        }

                        override fun onFailure(call : Call<News>, t : Throwable) {
                            Toast.makeText(baseContext, "Failed to load", Toast.LENGTH_SHORT).show()

                            swipe_to_refresh.isRefreshing = false
                        }
                    }
                )
            }
        }
    }

}