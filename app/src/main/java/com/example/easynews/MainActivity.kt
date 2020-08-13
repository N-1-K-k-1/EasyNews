package com.example.easynews

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easynews.`interface`.NewsService
import com.example.easynews.adapter.viewHolder.ListSourceAdapter
import com.example.easynews.common.Common
import com.example.easynews.model.WebSite
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    lateinit var layoutManager: LinearLayoutManager
    lateinit var mService: NewsService
    lateinit var adapter: ListSourceAdapter
    lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* Cache */
        Paper.init(this)

        /* Service */
        mService = Common.newsService

        /* View */
        swipe_to_refresh.setOnRefreshListener {
            loadWebSiteSource(true)
        }

        recycler_view_source_news.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recycler_view_source_news.layoutManager = layoutManager

        dialog = SpotsDialog.Builder().setContext(this).build()

        loadWebSiteSource(false)
    }

    private fun loadWebSiteSource(isRefreshed : Boolean) {

        // If not refreshed, then read cache, else load website and write cache
        if (!isRefreshed) {
            val cache = Paper.book().read<String>("cache")
            if (cache != null && !cache.isBlank() && cache != "null")
            {
                val webSite = Gson().fromJson<WebSite>(cache, WebSite::class.java)
                adapter =
                    ListSourceAdapter(
                        baseContext,
                        webSite
                    )
                adapter.notifyDataSetChanged()
                recycler_view_source_news.adapter = adapter
            }
            else
            {
                dialog.show()
                mService.sources.enqueue(object : retrofit2.Callback<WebSite> {
                    override fun onResponse(call : Call<WebSite>, response : Response<WebSite>) {
                        adapter = response.body()?.let {
                            ListSourceAdapter(
                                baseContext,
                                it
                            )
                        }!!
                        adapter.notifyDataSetChanged()
                        recycler_view_source_news.adapter = adapter

                        Paper.book().write("cache", Gson().toJson(response.body()))

                        dialog.dismiss()
                    }

                    override fun onFailure(call : Call<WebSite>, t : Throwable) {
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
                override fun onResponse(call : Call<WebSite>, response : Response<WebSite>) {
                    adapter = response.body()?.let {
                        ListSourceAdapter(
                            baseContext,
                            it
                        )
                    }!!
                    adapter.notifyDataSetChanged()
                    recycler_view_source_news.adapter = adapter

                    Paper.book().write("cache", Gson().toJson(response.body()))

                    swipe_to_refresh.isRefreshing = false
                }

                override fun onFailure(call : Call<WebSite>, t : Throwable) {
                    Toast.makeText(baseContext, "Failed to load", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}