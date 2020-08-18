package com.example.easynews

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easynews.`interface`.NewsService
import com.example.easynews.adapter.viewHolder.ListNewsAdapter
import com.example.easynews.common.Common
import com.example.easynews.model.News
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_list_news.*
import kotlinx.android.synthetic.main.activity_list_news.swipe_to_refresh
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ListNews : AppCompatActivity() {

    private var source = ""
    var webHotUrl: String? = ""

    lateinit var dialog: AlertDialog
    private lateinit var mService: NewsService
    lateinit var adapter: ListNewsAdapter

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_news)

        //View initialization
        mService = Common.newsService

        dialog = SpotsDialog.Builder().setContext(this).build()

        swipe_to_refresh.setOnRefreshListener { loadNews(source, true) }

        diagonalLayout.setOnClickListener {
            val detail = Intent(baseContext, NewsDetails::class.java)
            detail.putExtra("webUrl", webHotUrl)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                detail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(detail)
        }

        list_news.setHasFixedSize(true)
        list_news.layoutManager = LinearLayoutManager(this)

        if (intent != null) {
            source = intent.getStringExtra("source")!!
            if (source.isNotEmpty())
                loadNews(source, false)
        }
    }

    private fun loadNews(source: String?, isRefreshed: Boolean) {
        if (isRefreshed) {
            swipe_to_refresh.isRefreshing = true
            mService.getNewsFromSource(Common.getNewsAPI(source!!))
                .enqueue(object : Callback<News> {
                    override fun onFailure(call : Call<News>?, t : Throwable) {
                        Toast.makeText(baseContext, "Failed to load", Toast.LENGTH_SHORT).show()

                        swipe_to_refresh.isRefreshing = false
                    }

                    override fun onResponse(call: Call<News>?, response: Response<News>?) {
                        // Get first article to hot news
                        Picasso.get()
                            .load(response!!.body()!!.articles!![0].urlToImage)
                            .into(top_image)

                        top_title.text = response.body()!!.articles!![0].title
                        top_author.text = response.body()!!.articles!![0].author

                        webHotUrl = response.body()!!.articles!![0].url

                        // Load all articles that remains
                        val firstItemRemoved = response.body()!!.articles
                        firstItemRemoved!!.removeAt(0)

                        adapter = ListNewsAdapter(baseContext, firstItemRemoved)
                        adapter.notifyDataSetChanged()
                        list_news.adapter = adapter

                        swipe_to_refresh.isRefreshing = false
                    }

                })
        }
        else {
            dialog.show()
            mService.getNewsFromSource(Common.getNewsAPI(source!!))
                .enqueue(object : Callback<News> {
                    override fun onFailure(call : Call<News>?, t : Throwable) {
                        Toast.makeText(baseContext, "Failed to load", Toast.LENGTH_SHORT).show()

                        dialog.dismiss()
                    }

                    override fun onResponse(call: Call<News>?, response: Response<News>?) {
                        // Get first article to hot news
                        Picasso.get()
                            .load(response!!.body()!!.articles!![0].urlToImage)
                            .into(top_image)

                        top_title.text = response.body()!!.articles!![0].title
                        top_author.text = response.body()!!.articles!![0].author

                        webHotUrl = response.body()!!.articles!![0].url

                        // Load all articles that remains
                        val firstItemRemoved = response.body()!!.articles
                        firstItemRemoved!!.removeAt(0)

                        adapter = ListNewsAdapter(baseContext, firstItemRemoved)
                        adapter.notifyDataSetChanged()
                        list_news.adapter = adapter

                        dialog.dismiss()
                    }

                })
        }
    }
}