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
import com.example.easynews.model.Bookmark
import com.google.gson.internal.bind.util.ISO8601Utils
import com.squareup.picasso.Picasso
import java.text.ParseException
import java.text.ParsePosition
import java.util.*

class BookmarksAdapter(private val context : Context, private val bookmarkList: List<Bookmark>) : RecyclerView.Adapter<ListNewsViewHolder>() {
    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ListNewsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.news_layout, parent, false)

        return ListNewsViewHolder(itemView)
    }

    override fun getItemCount() : Int {
        return bookmarkList.size
    }

    override fun onBindViewHolder(holder: ListNewsViewHolder, position: Int) {

        // Load image
        if (bookmarkList[position].image == null) {
            Picasso.get().load(R.drawable.no_image_available).into(holder.articleImage)
        }
        else {
            Picasso.get()
                .load(bookmarkList[position].image)
                .error(R.drawable.no_image_available)
                .into(holder.articleImage)
        }

        if (bookmarkList[position].title!!.length > 65) {
            holder.articleTitle.text = bookmarkList[position].title!!.substring(0, 65).plus("...")
        }
        else {
            holder.articleTitle.text = bookmarkList[position].title!!
        }

        if (bookmarkList[position].publishedAt != null) {
            var date: Date? = null
            try {
                date = ISO8601Utils.parse(bookmarkList[position].publishedAt!!, ParsePosition(0))
            }
            catch (ex: ParseException) {
                ex.printStackTrace()
            }
            holder.articleTime.setReferenceTime(date!!.time)
        }

        //Set Event Click
        holder.setItemClickListener(object: ItemClickListener {
            override fun onClick(view : View, position : Int) {
                val detail = Intent(context, NewsDetails::class.java)
                detail.putExtra("webUrl", bookmarkList[position].url)
                detail.putExtra("title", bookmarkList[position].title)
                detail.putExtra("publishedAt", bookmarkList[position].publishedAt)
                detail.putExtra("image", bookmarkList[position].image)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    detail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(detail)
            }
        })
    }
}