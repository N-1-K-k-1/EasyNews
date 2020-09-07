package com.example.easynews

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.easynews.common.AppDatabase
import com.example.easynews.model.Bookmark
import com.example.easynews.network.AsyncTask.executeAsyncTask
import com.google.android.material.bottomnavigation.BottomNavigationView
import dmax.dialog.SpotsDialog
import org.adblockplus.libadblockplus.android.webview.AdblockWebView


class NewsDetails : AppCompatActivity() {

    private lateinit var dialog : AlertDialog
    private lateinit var toolbar: ActionBar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var menu: Menu
    private lateinit var menuItem: MenuItem
    private lateinit var db: AppDatabase
    private lateinit var adblockWebView: AdblockWebView
    private lateinit var hiddenPanel: ViewGroup
    private lateinit var webPage: ViewGroup
    private lateinit var root: ViewGroup

    private var isPanelShown = false
    private var isChecked = false
    private var screenHeight = 0

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.settings_control -> {
               /* when (adblockWebView.settings.textZoom) {
                    100 -> adblockWebView.settings.textZoom = 120
                    120 -> adblockWebView.settings.textZoom = 140
                    140 -> adblockWebView.settings.textZoom = 160
                    160 -> adblockWebView.settings.textZoom = 180
                    180 -> adblockWebView.settings.textZoom = 200
                }*/
                slideUpDown()

                return@OnNavigationItemSelectedListener true
            }
            R.id.bookmarks_control -> {
                addToBookmarks()

                return@OnNavigationItemSelectedListener true
            }
            R.id.share_control -> {
                shareTheArticle()

                return@OnNavigationItemSelectedListener true
            }
            else -> {
                false
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_details)

        /* Loading dialog */
        dialog = SpotsDialog.Builder().setContext(this).build()
        dialog.show()

        /* Top action bar */
        toolbar = supportActionBar!!
        toolbar.hide()

        /* Bottom navigation */
        bottomNav = findViewById(R.id.navigationWebView)
        bottomNav.setOnNavigationItemSelectedListener(navListener)
        menu = bottomNav.menu

        /* Changing menu items */
        for (i in 0 until menu.size()) {
            menuItem = menu.getItem(i)
            menuItem.isCheckable = false
        }

        /* DataBase initialization */
        db = AppDatabase.getInstance(this)

        /* Checking bookmarks */
        checkBookmarks()

        //WebView
        adblockWebView = findViewById(R.id.webView)
        adblockWebView.settings.javaScriptEnabled = true
        adblockWebView.webChromeClient = WebChromeClient()
        adblockWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view : WebView?, url : String?) {
                dialog.dismiss()
            }
        }

        if (intent.getStringExtra("webUrl") != null) {
            if (intent.getStringExtra("webUrl")!!.isNotEmpty())
                adblockWebView.loadUrl(intent.getStringExtra("webUrl")!!)
        }

        webPage = findViewById(R.id.webView)
        root = findViewById(R.id.web)
        val vto = webPage.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                screenHeight = webPage.height
                webPage.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        hiddenPanel = layoutInflater.inflate(R.layout.settings_control_layout, root, false) as ViewGroup
        hiddenPanel.visibility = View.INVISIBLE
        root.addView(hiddenPanel)


        isPanelShown = false
    }

    private fun addToBookmarks() {
        val bookmark = intent.getStringExtra("webUrl")?.let {
            Bookmark(
                url = it,
                title = intent.getStringExtra("title"),
                publishedAt = intent.getStringExtra("publishedAt"),
                image = intent.getStringExtra("image")
            )
        }
        isChecked = if (!isChecked) {
            bottomNav.menu.findItem(R.id.bookmarks_control).setIcon(R.drawable.ic_baseline_bookmark_24)
            lifecycleScope.executeAsyncTask(onPreExecute = {
                // ... runs in Main Thread
            }, doInBackground = {
                if (bookmark != null) {
                    db.bookmarkDao().insertBookmark(bookmark)
                    println(db.bookmarkDao().getAll())
                }
            }, onPostExecute = {
                // runs in Main Thread
                // ... here "it" is the data returned from "doInBackground"
            })

            true
        } else {
            bottomNav.menu.findItem(R.id.bookmarks_control).setIcon(R.drawable.no_bookmark)
            lifecycleScope.executeAsyncTask(onPreExecute = {
                // ... runs in Main Thread
            }, doInBackground = {
                intent.getStringExtra("webUrl")?.let { db.bookmarkDao().deleteBookmark(it) }
                println(db.bookmarkDao().getAll())
            }, onPostExecute = {
                // runs in Main Thread
                // ... here "it" is the data returned from "doInBackground"
            })

            false
        }
    }

    private fun checkBookmarks() {
        if (intent.getStringExtra("webUrl") != null) {
            lifecycleScope.executeAsyncTask(onPreExecute = {
                // ... runs in Main Thread
            }, doInBackground = {
                if (db.bookmarkDao().findByUrl(intent.getStringExtra("webUrl")!!) != null) {
                    bottomNav.menu.findItem(R.id.bookmarks_control).setIcon(R.drawable.ic_baseline_bookmark_24)
                    isChecked = true
                }
            }, onPostExecute = {
                // runs in Main Thread
                // ... here "it" is the data returned from "doInBackground"
            })
        }
    }

    private fun shareTheArticle() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody = getString(R.string.share_body_template_first)
                .plus("\n\n")
                .plus(intent.getStringExtra("webUrl"))
                .plus("\n\n")
                .plus(getString(R.string.share_body_template_last))
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)))
    }

    fun slideUpDown() {
        if (!isPanelShown) {
            // Show the panel
            webPage.layout(
                webPage.left,
                webPage.top - screenHeight * 25 / 100,
                webPage.right,
                webPage.bottom - screenHeight * 25 / 100
            )
            hiddenPanel.layout(
                webPage.left,
                webPage.bottom,
                webPage.right,
                screenHeight
            )
            hiddenPanel.visibility = View.VISIBLE
            val bottomUp: Animation = AnimationUtils.loadAnimation(
                this,
                R.anim.bottom_up
            )
            hiddenPanel.startAnimation(bottomUp)
            isPanelShown = true
        } else {
            isPanelShown = false

            // Hide the Panel
            val bottomDown: Animation = AnimationUtils.loadAnimation(
                this,
                R.anim.bottom_down
            )
            bottomDown.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(arg0: Animation) {
                    // TODO Auto-generated method stub
                }

                override fun onAnimationRepeat(arg0: Animation) {
                    // TODO Auto-generated method stub
                }

                override fun onAnimationEnd(arg0: Animation) {
                    isPanelShown = false
                    webPage.layout(
                        webPage.left,
                        webPage.top + screenHeight * 25 / 100,
                        webPage.right,
                        webPage.bottom + screenHeight * 25 / 100
                    )
                    hiddenPanel.layout(
                        webPage.left,
                        webPage.bottom,
                        webPage.right,
                        screenHeight
                    )
                }
            })
            hiddenPanel.startAnimation(bottomDown)
        }
    }
}