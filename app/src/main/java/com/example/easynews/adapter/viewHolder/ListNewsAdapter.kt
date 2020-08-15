package com.example.easynews.adapter.viewHolder

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.easynews.NewsDetails
import com.example.easynews.R
import com.example.easynews.`interface`.ItemClickListener
import com.example.easynews.model.Article
import com.google.gson.internal.bind.util.ISO8601Utils
import com.squareup.picasso.Picasso
import java.text.ParseException
import java.text.ParsePosition
import java.util.*

class ListNewsAdapter(private val context : Context, private val articleList: MutableList<Article>) : RecyclerView.Adapter<ListNewsViewHolder>() {
    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ListNewsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.news_layout, parent, false)

        return ListNewsViewHolder(itemView)
    }

    override fun getItemCount() : Int {
        return articleList.size
    }

    override fun onBindViewHolder(holder : ListNewsViewHolder, position : Int) {
        // Load image
        Picasso.get().load(articleList[position].urlToImage).into(holder.article_image)
        if (articleList[position].title!!.length > 65) {
            holder.article_title.text = articleList[position].title!!.substring(0, 65).plus("...")
        }
        else {
            holder.article_title.text = articleList[position].title!!
        }

        if (articleList[position].publishedAt != null) {
            var date: Date? = null
            try {
                date = ISO8601Utils.parse(articleList[position].publishedAt!!, ParsePosition(0))
            }
            catch (ex: ParseException) {
                ex.printStackTrace()
            }
            holder.article_time.setReferenceTime(date!!.time)
        }

        //Set Event Click
        holder.setItemClickListener(object: ItemClickListener{
            override fun onClick(view : View, position : Int) {
                val detail = Intent(context, NewsDetails::class.java)
                detail.putExtra("webUrl", articleList[position].url)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    detail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(detail)
            }

        })
    }

}