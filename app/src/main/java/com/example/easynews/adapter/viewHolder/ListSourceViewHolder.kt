package com.example.easynews.adapter.viewHolder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.easynews.`interface`.ItemClickListener
import kotlinx.android.synthetic.main.source_news.view.*

class ListSourceViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private lateinit var  itemClickListener: ItemClickListener

    var sourceTitle: TextView = itemView.source_news_name

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onClick(p0: View?) {
        itemClickListener.onClick(p0!!, adapterPosition)
    }
}