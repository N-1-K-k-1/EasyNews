package com.example.easynews

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easynews.`interface`.NewsService
import com.example.easynews.adapter.viewHolder.ListNewsAdapter
import com.example.easynews.common.Common
import com.example.easynews.model.News
import com.example.easynews.network.AsyncTask.executeAsyncTask
import com.google.android.material.bottomnavigation.BottomNavigationView
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_search_news.*
import retrofit2.Call
import retrofit2.Response
import java.net.URL

class SearchNews : AppCompatActivity() {

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var mService: NewsService
    private lateinit var adapter: ListNewsAdapter
    private lateinit var dialog: AlertDialog
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var menu: Menu
    private lateinit var menuItem: MenuItem
    private lateinit var toolbar: androidx.appcompat.app.ActionBar
    private lateinit var searchManager: SearchManager
    private lateinit var searchItem: MenuItem
    private lateinit var searchView: SearchView

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_local -> {
                val globalIntent = Intent(baseContext, MainActivity::class.java)
                globalIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(globalIntent)

                return@OnNavigationItemSelectedListener  true
            }
            R.id.navigation_global -> {
                val globalIntent = Intent(baseContext, ListNews::class.java)
                globalIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(globalIntent)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {

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

        setContentView(R.layout.activity_search_news)

        /* Service */
        mService = Common.newsService

        /* Bottom navigation */
        bottomNav = findViewById(R.id.navigationView)
        bottomNav.setOnNavigationItemSelectedListener(navListener)
        menu = bottomNav.menu

        /* Top action bar */
        toolbar = supportActionBar!!

        /* View */
        list_news.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        list_news.layoutManager = layoutManager

        dialog = SpotsDialog.Builder().setContext(this).build()
    }

    override fun onStart() {
        super.onStart()

        /* Changing menu item */
        menuItem = menu.getItem(2)
        menuItem.isChecked = true

        /* Changing title in ActionBar */
        toolbar.title = getString(R.string.search)
        toolbar.isHideOnContentScrollEnabled = true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)

        searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchItem = menu?.findItem(R.id.search)!!
        searchView = searchItem.actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                searchView.setQuery("", false)
                searchItem.collapseActionView()
                query?.let { loadNewsFromSearch(it) }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }


        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun loadNewsFromSearch(searchValue: String) {

        mService.getNewsFromSearch(searchValue).enqueue(
            object : retrofit2.Callback<News> {
                override fun onResponse(call: Call<News>, response: Response<News>) {
                    var cnt = 0

                    lifecycleScope.executeAsyncTask(onPreExecute = {
                        // ... runs in Main Thread
                    }, doInBackground = {
                        val regex = Regex(pattern = """\d+""")
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

                    adapter = response.body()?.articles?.let { ListNewsAdapter(baseContext, it) }!!
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