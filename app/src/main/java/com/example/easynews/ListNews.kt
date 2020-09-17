package com.example.easynews

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easynews.`interface`.NewsService
import com.example.easynews.adapter.viewHolder.ListNewsAdapter
import com.example.easynews.common.Common
import com.example.easynews.common.GeoData
import com.example.easynews.model.News
import com.example.easynews.network.AsyncTask.executeAsyncTask
import com.example.easynews.network.GlobalVars
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_list_news.*
import kotlinx.android.synthetic.main.activity_list_news.diagonalLayout
import kotlinx.android.synthetic.main.activity_list_news.list_news
import kotlinx.android.synthetic.main.activity_list_news.swipe_to_refresh
import kotlinx.android.synthetic.main.activity_list_news.top_author
import kotlinx.android.synthetic.main.activity_list_news.top_image
import kotlinx.android.synthetic.main.activity_list_news.top_title
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.net.URL


class ListNews : AppCompatActivity() {
    private var topUrl: String? = ""

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var mService: NewsService
    private lateinit var adapter: ListNewsAdapter
    private lateinit var dialog: AlertDialog
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var menu: Menu
    private lateinit var menuItem: MenuItem
    private lateinit var toolbar: androidx.appcompat.app.ActionBar
    private lateinit var geoData: GeoData

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_local -> {
                val globalIntent = Intent(baseContext, MainActivity::class.java)
                globalIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(globalIntent)

                return@OnNavigationItemSelectedListener  true
            }
            R.id.navigation_global -> {
                loadGlobalNews(true)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                val globalIntent = Intent(baseContext, SearchNews::class.java)
                globalIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(globalIntent)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_bookmarks -> {
                val globalIntent = Intent(baseContext, Bookmarks::class.java)
                globalIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(globalIntent)

                return@OnNavigationItemSelectedListener true
            }
            else -> {
                false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(AppCompatDelegate.FEATURE_SUPPORT_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_news)

        /* Service */
        mService = Common.newsService

        /* Bottom navigation */
        bottomNav = findViewById(R.id.navigationView)
        bottomNav.setOnNavigationItemSelectedListener(navListener)
        menu = bottomNav.menu

        /* Top action bar */
        toolbar = supportActionBar!!

        /* Detect location and check permissions */
        geoData = GeoData(this)
        geoData.checkLocation()

        /* View */
        swipe_to_refresh.setOnRefreshListener {
            loadGlobalNews(true)
        }

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

        loadGlobalNews(false)
    }

    override fun onStart() {
        super.onStart()

        /* Changing menu item */
        menuItem = menu.getItem(1)
        menuItem.isChecked = true

        /* Changing title in ActionBar */
        toolbar.title = getString(R.string.global_news_actionBar)
        toolbar.isHideOnContentScrollEnabled = true
    }

    private fun loadGlobalNews(isRefreshed: Boolean) {
        if (!isRefreshed) {

            GlobalVars.counter++

            val cache = Paper.book().read<String>("cacheGlobal")

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
                mService.getGlobalNews(geoData.getCountry()).enqueue(
                    object : retrofit2.Callback<News> {
                        override fun onResponse(call: Call<News>, response: Response<News>) {
                            var cnt = 0
                            val regex = Regex(pattern = """\d+""")

                            lifecycleScope.executeAsyncTask(onPreExecute = {
                                // ... runs in Main Thread
                            }, doInBackground = {
                                for (i in 0 until response.body()!!.articles?.size!!) {
                                    if (response.body()!!.articles?.get(i)?.title?.let { it1 ->
                                            regex.matches(it1)
                                        }!!) {
                                        response.body()!!.articles?.get(i)?.title = response.body()!!.articles?.get(i)?.description
                                    }
                                    try {
                                        URL(response.body()!!.articles?.get(i)?.urlToImage)
                                        response.body()!!.articles?.get(i)?.isImage  = true
                                    } catch (e: Exception) {
                                        cnt++
                                    }
                                }
                                // ... runs in Worker(Background) Thread
                                cnt.toString() // send data to "onPostExecute"
                            }, onPostExecute = { it ->
                                println("We lost $it images")
                                // runs in Main Thread
                                // ... here "it" is the data returned from "doInBackground"
                            })

                            Picasso.get()
                                .load(response.body()?.articles?.get(0)?.urlToImage)
                                .into(top_image)

                            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                            StrictMode.setThreadPolicy(policy)

                            lifecycleScope.executeAsyncTask(onPreExecute = {
                                // ... runs in Main Thread
                            }, doInBackground = {
                                try {
                                    URL(response.body()?.articles?.get(0)?.urlToImage).readBytes()
                                } catch (e: IOException) {
                                    Picasso.get().load(R.drawable.no_image_available).into(top_image)
                                }
                            }, onPostExecute = { it ->
                                // runs in Main Thread
                                // ... here "it" is the data returned from "doInBackground"
                            })

                            top_title.text = response.body()!!.articles!![0].title

                            if (response.body()!!.articles?.get(0)?.author != null) {
                                if (response.body()!!.articles!![0].author?.let { it1 ->
                                        regex.matches(it1)
                                    }!!)
                                    top_author.text = ""
                                else
                                    top_author.text = response.body()!!.articles!![0].author
                            }

                            topUrl = response.body()!!.articles!![0].url

                            Paper.book().write("cacheGlobal", Gson().toJson(response.body()!!))

                            // Load all articles that remains
                            val firstItemRemoved = response.body()!!.articles
                            firstItemRemoved!!.removeAt(0)

                            adapter = ListNewsAdapter(baseContext, firstItemRemoved)
                            adapter.notifyDataSetChanged()
                            list_news.adapter = adapter

                            dialog.dismiss()
                        }

                        override fun onFailure(call: Call<News>, t: Throwable) {
                            Toast.makeText(baseContext, getString(R.string.failedToLoad), Toast.LENGTH_SHORT).show()

                            dialog.dismiss()
                        }
                    }
                )
            }
        }
        else
        {
            swipe_to_refresh.isRefreshing = true

            // Fetch new data
            mService.getGlobalNews(geoData.getCountry()).enqueue(
                object : retrofit2.Callback<News> {
                    override fun onResponse(call: Call<News>, response: Response<News>) {
                        var cnt = 0
                        val regex = Regex(pattern = """\d+""")

                        lifecycleScope.executeAsyncTask(onPreExecute = {
                            // ... runs in Main Thread
                        }, doInBackground = {
                            for (i in 0 until response.body()!!.articles?.size!!) {
                                if (response.body()!!.articles?.get(i)?.title?.let { it1 ->
                                        regex.matches(it1)
                                    }!!) {
                                    response.body()!!.articles?.get(i)?.title = response.body()!!.articles?.get(i)?.description
                                }
                                try {
                                    URL(response.body()!!.articles?.get(i)?.urlToImage)
                                    response.body()!!.articles?.get(i)?.isImage  = true
                                } catch (e: Exception) {
                                    response.body()!!.articles?.get(i)?.isImage  = false
                                    cnt++
                                }
                            }
                            // ... runs in Worker(Background) Thread
                            cnt.toString() // send data to "onPostExecute"
                        }, onPostExecute = { it ->
                            println("We lost $it images")
                            // runs in Main Thread
                            // ... here "it" is the data returned from "doInBackground"
                        })

                        Picasso.get()
                            .load(response.body()?.articles?.get(0)?.urlToImage)
                            .into(top_image)

                        lifecycleScope.executeAsyncTask(onPreExecute = {
                            // ... runs in Main Thread
                        }, doInBackground = {
                            try {
                                URL(response.body()?.articles?.get(0)?.urlToImage).readBytes()
                            } catch (e: IOException) {
                                Picasso.get().load(R.drawable.no_image_available).into(top_image)
                            }
                        }, onPostExecute = { it ->
                            // runs in Main Thread
                            // ... here "it" is the data returned from "doInBackground"
                        })

                        top_title.text = response.body()!!.articles!![0].title

                        if (response.body()!!.articles?.get(0)?.author != null) {
                            if (response.body()!!.articles!![0].author?.let { it1 ->
                                    regex.matches(it1)
                                }!!)
                                top_author.text = ""
                            else
                                top_author.text = response.body()!!.articles!![0].author
                        }

                        topUrl = response.body()!!.articles!![0].url

                        Paper.book().write("cacheGlobal", Gson().toJson(response.body()!!))

                        val firstItemRemoved = response.body()!!.articles
                        firstItemRemoved!!.removeAt(0)

                        adapter = ListNewsAdapter(baseContext, firstItemRemoved)
                        adapter.notifyDataSetChanged()
                        list_news.adapter = adapter

                        swipe_to_refresh.isRefreshing = false
                    }

                    override fun onFailure(call : Call<News>, t : Throwable) {
                        Toast.makeText(baseContext, getString(R.string.failedToLoad), Toast.LENGTH_SHORT).show()

                        swipe_to_refresh.isRefreshing = false
                    }
                }
            )
        }
    }
}