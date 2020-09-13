package com.example.easynews

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.example.easynews.common.AppDatabase
import com.example.easynews.model.Bookmark
import com.example.easynews.network.AsyncTask.executeAsyncTask
import com.google.android.material.bottomnavigation.BottomNavigationView
import dmax.dialog.SpotsDialog
import io.paperdb.Paper
import kotlinx.android.synthetic.main.settings_control_layout.*
import org.adblockplus.libadblockplus.android.webview.AdblockWebView

class NewsDetails : AppCompatActivity() {

    private lateinit var dialog : AlertDialog
    private lateinit var toolbar: ActionBar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var menu: Menu
    private lateinit var menuItem: MenuItem
    private lateinit var db: AppDatabase
    private lateinit var adblockWebView: AdblockWebView
    private lateinit var hiddenPanel: View
    private lateinit var root: ViewGroup
    private lateinit var settingsControlPanel: View
    private lateinit var transition: Transition
    private lateinit var fontSizeText: TextView

    private var isPanelShown = true
    private var isChecked = false

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.settings_control -> {
                // If app has no permission to write system settings
                if(!canWrite){
                    bar_layout.visibility = View.GONE
                    divider.visibility = View.GONE

                    showAlertMsg()
                }
                else {
                    bar_layout.visibility = View.VISIBLE
                    divider.visibility = View.VISIBLE
                }

                toggle(isPanelShown)

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

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
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

        /* WebView */
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


        /* Creating settings_control_panel View */
        root = findViewById(R.id.web)
        hiddenPanel = layoutInflater.inflate(R.layout.settings_control_layout, root, false)
        root.addView(hiddenPanel)

        root.setOnClickListener { toggle(true) }

        settingsControlPanel = findViewById(R.id.settings_control_panel)
        transition = Slide(Gravity.BOTTOM)

        fontSizeText = findViewById(R.id.font_size_text)
        // Changing font size
        val cache = Paper.book().read<Int>("fontSizeCache")
        if (cache != null && cache != 0) {
            when (cache) {
                70 -> {
                    fontSizeText.text = getString(R.string.little_font)
                    fontSizeText.textSize = 16F
                    fontSizeText.setPadding(0,5,0,0)
                    adblockWebView.settings.textZoom = 70
                }
                100 -> {
                    fontSizeText.text = getString(R.string.normal_font)
                    fontSizeText.textSize = 18F
                    adblockWebView.settings.textZoom = 100
                }
                130 -> {
                    fontSizeText.text = getString(R.string.large_font)
                    fontSizeText.textSize = 20F
                    adblockWebView.settings.textZoom = 130
                }
                160 -> {
                    fontSizeText.text = getString(R.string.extra_font)
                    fontSizeText.textSize = 22F
                    adblockWebView.settings.textZoom = 160
                }
            }
            adblockWebView.settings.textZoom = cache
        }
        else {
            fontSizeText.text = getString(R.string.normal_font)
        }
        hiddenPanel.findViewById<View>(R.id.button_minus).setOnClickListener {
            when (adblockWebView.settings.textZoom) {
                100 -> {
                    fontSizeText.text = getString(R.string.little_font)
                    fontSizeText.textSize = 16F
                    fontSizeText.setPadding(0,5,0,0)
                    adblockWebView.settings.textZoom = 70
                    Paper.book().write("fontSizeCache", adblockWebView.settings.textZoom)
                }
                130 -> {
                    fontSizeText.text = getString(R.string.normal_font)
                    fontSizeText.textSize = 18F
                    adblockWebView.settings.textZoom = 100
                    Paper.book().write("fontSizeCache", adblockWebView.settings.textZoom)
                }
                160 -> {
                    fontSizeText.text = getString(R.string.large_font)
                    fontSizeText.textSize = 20F
                    adblockWebView.settings.textZoom = 130
                    Paper.book().write("fontSizeCache", adblockWebView.settings.textZoom)
                }
            }
        }
        hiddenPanel.findViewById<View>(R.id.button_plus).setOnClickListener {
            when (adblockWebView.settings.textZoom) {
                70 -> {
                    fontSizeText.text = getString(R.string.normal_font)
                    fontSizeText.textSize = 18F
                    fontSizeText.setPadding(0,0,0,0)
                    adblockWebView.settings.textZoom = 100
                    Paper.book().write("fontSizeCache", adblockWebView.settings.textZoom)
                }
                100 -> {
                    fontSizeText.text = getString(R.string.large_font)
                    fontSizeText.textSize = 20F
                    adblockWebView.settings.textZoom = 130
                    Paper.book().write("fontSizeCache", adblockWebView.settings.textZoom)
                }
                130 -> {
                    fontSizeText.text = getString(R.string.extra_font)
                    fontSizeText.textSize = 22F
                    adblockWebView.settings.textZoom = 160
                    Paper.book().write("fontSizeCache", adblockWebView.settings.textZoom)
                }
            }
        }

        // Set the SeekBar initial progress from screen current brightness
        val brightness = brightness
        brightness_bar.progress = brightness
    }

    override fun onStart() {
        super.onStart()

        // If app has no permission to write system settings
        if(!canWrite){
            bar_layout.visibility = View.GONE
            divider.visibility = View.GONE
        }
        else {
            bar_layout.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
        }

        // Set a SeekBar change listener
        brightness_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Change the screen brightness
                if (canWrite){
                    setBrightness(i)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (!inViewInBounds(settingsControlPanel, event.rawX.toInt(), event.rawY.toInt())) {
                toggle(false)
            }
        }
        return super.dispatchTouchEvent(event)
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
                @Suppress("SENSELESS_COMPARISON")
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

    private fun toggle(show: Boolean) {
        isPanelShown = !show
        if (show)
            hiddenPanel.minimumHeight = bottomNav.height
        transition.duration = 200
        transition.addTarget(R.id.settings_control_panel)
        TransitionManager.beginDelayedTransition(root, transition)
        settingsControlPanel.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun inViewInBounds(view: View, x: Int, y: Int): Boolean {
        val outRect = Rect()
        val location = IntArray(2)
        view.getDrawingRect(outRect)
        view.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        return outRect.contains(x, y)
    }

    // Extension property to get write system settings permission status
    val Context.canWrite: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.System.canWrite(this)
            } else {
                true
            }
        }


    // Extension property to get screen brightness programmatically
    private val Context.brightness: Int
        get() {
            return Settings.System.getInt(
                this.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                0
            )
        }


    // Extension method to set screen brightness programmatically
    fun Context.setBrightness(value:Int) {
        Settings.System.putInt(
            this.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            value
        )
    }

    private fun showAlertMsg() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(getString(R.string.write_permission))
        dialog.setPositiveButton("Settings") { _, _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
        dialog.setNegativeButton(getString(R.string.cancel)) { _, _ ->
            return@setNegativeButton
        }
        dialog.setCancelable(false)
        dialog.show()
    }

}