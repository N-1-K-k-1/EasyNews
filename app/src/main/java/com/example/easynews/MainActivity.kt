package com.example.easynews

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
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
import java.net.URL

class MainActivity : AppCompatActivity() {

    var topUrl: String = ""

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var mService: NewsService
    lateinit var adapter: ListNewsAdapter
    lateinit var dialog: AlertDialog
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var toolbar: androidx.appcompat.app.ActionBar
    private lateinit var geoData: GeoData

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_local -> {
                if (geoData.getCityName() != null)
                    toolbar.title = getString(R.string.local_news_for) + " " + geoData.getCityName()
                else
                    toolbar.title = getString(R.string.local_news_for)

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

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(AppCompatDelegate.FEATURE_SUPPORT_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)

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

        /* Detect location and check permissions */
        geoData = GeoData(this, savedInstanceState)
        geoData.checkLocation()


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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    recreate()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.permission),
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (geoData.getCityName() != null)
            toolbar.title = getString(R.string.local_news_for) + " " + geoData.getCityName()
        else
            toolbar.title = getString(R.string.local_news_for)

        toolbar.isHideOnContentScrollEnabled = true
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
                    mService.getLocalNews("$it OR ${it}е OR ${it}а OR ${it.substring(0, (it.length-1))}ы", geoData.getLanguage()).enqueue(
                        object : retrofit2.Callback<News> {
                            override fun onResponse(call: Call<News>, response: Response<News>) {
                                var cnt = 0

                                lifecycleScope.executeAsyncTask(onPreExecute = {
                                    // ... runs in Main Thread
                                }, doInBackground = {
                                    for (i in 0 until response.body()!!.articles?.size!!) {
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

                                try {
                                    URL(response.body()?.articles?.get(0)?.urlToImage).readBytes()
                                } catch (e: IOException) {
                                    Picasso.get().load(R.drawable.no_image_available).into(top_image)
                                }

                                top_title.text = response.body()!!.articles!![0].title
                                top_author.text = response.body()!!.articles!![0].author
                                topUrl = response.body()!!.articles!![0].url.toString()

                                Paper.book().write("cache", Gson().toJson(response.body()!!))

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
        }
        else
        {
            swipe_to_refresh.isRefreshing = true

            // Fetch new data
            geoData.getCityName()?.let {
                mService.getLocalNews("$it OR ${it}е OR ${it}а OR ${it.substring(0, (it.length-1))}ы", geoData.getLanguage()).enqueue(
                    object : retrofit2.Callback<News> {
                        override fun onResponse(call: Call<News>, response: Response<News>) {
                            var cnt = 0

                            lifecycleScope.executeAsyncTask(onPreExecute = {
                                // ... runs in Main Thread
                            }, doInBackground = {
                                for (i in 0 until response.body()!!.articles?.size!!) {
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

                            try {
                                URL(response.body()?.articles?.get(0)?.urlToImage).readBytes()
                            } catch (e: IOException) {
                                Picasso.get().load(R.drawable.no_image_available).into(top_image)
                            }

                            top_title.text = response.body()!!.articles!![0].title
                            top_author.text = response.body()!!.articles!![0].author
                            topUrl = response.body()!!.articles!![0].url.toString()

                            Paper.book().write("cache", Gson().toJson(response.body()!!))

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

}