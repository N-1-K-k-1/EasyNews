package com.example.easynews

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easynews.adapter.viewHolder.BookmarksAdapter
import com.example.easynews.common.AppDatabase
import com.example.easynews.model.Bookmark
import com.example.easynews.network.AsyncTask.executeAsyncTask
import com.google.android.material.bottomnavigation.BottomNavigationView
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_bookmarks.*

class Bookmarks : AppCompatActivity() {

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: BookmarksAdapter
    private lateinit var dialog: AlertDialog
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var menu: Menu
    private lateinit var menuItem: MenuItem
    private lateinit var toolbar: androidx.appcompat.app.ActionBar
    private lateinit var relativeLayout: RelativeLayout
    private lateinit var db: AppDatabase
    private lateinit var bookmarks: List<Bookmark>

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
                val globalIntent = Intent(baseContext, SearchNews::class.java)
                globalIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(globalIntent)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_bookmarks -> {

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

        setContentView(R.layout.activity_bookmarks)

        /* Bottom navigation */
        bottomNav = findViewById(R.id.navigationView)
        bottomNav.setOnNavigationItemSelectedListener(navListener)
        menu = bottomNav.menu

        /* Top action bar */
        toolbar = supportActionBar!!

        /* DataBase initialization */
        db = AppDatabase.getInstance(this)

        /* View */
        list_news.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        list_news.layoutManager = layoutManager

        dialog = SpotsDialog.Builder().setContext(this).build()
    }

    override fun onStart() {
        super.onStart()

        /* Changing menu item */
        menuItem = menu.getItem(3)
        menuItem.isChecked = true

        /* Changing title in ActionBar */
        toolbar.title = getString(R.string.bookmarks)
        toolbar.isHideOnContentScrollEnabled = true

        /* Loading bookmarks */
        loadBookmarks()
    }

    private fun loadBookmarks() {
        lifecycleScope.executeAsyncTask(onPreExecute = {
            // ... runs in Main Thread
            relativeLayout = findViewById(R.id.no_bookmarks)
        }, doInBackground = {
            bookmarks = db.bookmarkDao().getAll()
        }, onPostExecute = {
            // runs in Main Thread
            // ... here "it" is the data returned from "doInBackground"
            if (bookmarks.isEmpty()) {
                relativeLayout.visibility = View.VISIBLE
                adapter = BookmarksAdapter(baseContext, bookmarks)
                adapter.notifyDataSetChanged()
                list_news.adapter = adapter
            }
            else {
                relativeLayout.visibility = View.INVISIBLE
                adapter = BookmarksAdapter(baseContext, bookmarks)
                adapter.notifyDataSetChanged()
                list_news.adapter = adapter

                dialog.dismiss()
            }
        })
    }
}