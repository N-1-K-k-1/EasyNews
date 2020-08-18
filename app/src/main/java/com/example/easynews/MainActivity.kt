package com.example.easynews

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easynews.`interface`.NewsService
import com.example.easynews.adapter.viewHolder.ListSourceAdapter
import com.example.easynews.common.Common
import com.example.easynews.model.WebSite
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Response
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.lang.StringBuilder
import com.example.easynews.common.CityDetect

class MainActivity : AppCompatActivity() {
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var mService: NewsService
    lateinit var adapter: ListSourceAdapter
    lateinit var dialog: AlertDialog
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var toolbar: androidx.appcompat.app.ActionBar
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var cityDetect: CityDetect
    private lateinit var city: String

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET
            ), 1)
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                cityDetect = CityDetect(baseContext)
                city = cityDetect.locateCity(location!!.latitude, location.longitude)
                Log.d("Location", city)
            }
        toolbar = supportActionBar!!
        toolbar.title = "Local news"
        toolbar.isHideOnContentScrollEnabled = true

        bottomNav = findViewById(R.id.navigationView)
        bottomNav.setOnNavigationItemSelectedListener(navListener)

        /* Cache */
        Paper.init(this)

        /* Service */
        mService = Common.newsService

        /* View */
        swipe_to_refresh.setOnRefreshListener { loadWebSiteSource(true) }

        recycler_view_source_news.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recycler_view_source_news.layoutManager = layoutManager

        dialog = SpotsDialog.Builder().setContext(this).build()

        loadWebSiteSource(false)
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener {item ->
        when (item.itemId) {
            R.id.navigation_local -> {
                toolbar.title = "Local news"
                if (city != "")
                    toolbar.title = "Local news for $city"
                return@OnNavigationItemSelectedListener  true
            }
            R.id.navigation_global -> {
                toolbar.title = "Global news"
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
            val cache = Paper.book().read<String>("cache")
            if (cache != null && !cache.isBlank() && cache != "null")
            {
                val webSite = Gson().fromJson<WebSite>(cache, WebSite::class.java)
                adapter =
                    ListSourceAdapter(baseContext, webSite)
                adapter.notifyDataSetChanged()
                recycler_view_source_news.adapter = adapter
            }
            else
            {
                dialog.show()
                mService.sources.enqueue(object : retrofit2.Callback<WebSite> {
                    override fun onResponse(call: Call<WebSite>, response: Response<WebSite>) {
                        adapter = response.body()?.let { ListSourceAdapter(baseContext, it) }!!
                        adapter.notifyDataSetChanged()
                        recycler_view_source_news.adapter = adapter

                        Paper.book().write("cache", Gson().toJson(response.body()))

                        dialog.dismiss()
                    }

                    override fun onFailure(call: Call<WebSite>, t: Throwable) {
                        Toast.makeText(baseContext, "Failed to load", Toast.LENGTH_SHORT).show()

                        dialog.dismiss()
                    }
                })
            }
        }
        else
        {
            swipe_to_refresh.isRefreshing = true
            // Fetch new data
            mService.sources.enqueue(object : retrofit2.Callback<WebSite> {
                override fun onResponse(call: Call<WebSite>, response: Response<WebSite>) {
                    adapter = response.body()?.let { ListSourceAdapter(baseContext, it) }!!
                    adapter.notifyDataSetChanged()
                    recycler_view_source_news.adapter = adapter

                    Paper.book().write("cache", Gson().toJson(response.body()))

                    swipe_to_refresh.isRefreshing = false
                }

                override fun onFailure(call : Call<WebSite>, t : Throwable) {
                    Toast.makeText(baseContext, "Failed to load", Toast.LENGTH_SHORT).show()

                    swipe_to_refresh.isRefreshing = false
                }
            })
        }
    }
}