package com.example.easynews.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.easynews.R
import com.example.easynews.`interface`.ItemClickListener
import com.example.easynews.adapter.viewHolder.ListSourceViewHolder
import com.example.easynews.model.WebSite

class ListSourceAdapter(private val context: Context, private val webSite : WebSite) : RecyclerView.Adapter<ListSourceViewHolder>() {
    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ListSourceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.source_news, parent, false)

        return ListSourceViewHolder(itemView)
    }

    override fun getItemCount() : Int {
        return webSite.sources!!.size
    }

    override fun onBindViewHolder(holder : ListSourceViewHolder, position : Int) {
        holder.sourceTitle.text = webSite.sources!![position].name
        holder.setItemClickListener(object : ItemClickListener {
            override fun onClick(view : View, position : Int) {
                Toast.makeText(context, "Will be implemented", Toast.LENGTH_SHORT).show()
            }
        })
    }
}