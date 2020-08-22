package com.example.easynews.adapter.viewHolder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.easynews.`interface`.ItemClickListener
import com.github.curioustechizen.ago.RelativeTimeTextView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.news_layout.view.*

class ListNewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private lateinit var  itemClickListener : ItemClickListener

    var articleTitle : TextView = itemView.article_title
    var articleTime : RelativeTimeTextView = itemView.article_time
    var articleImage : CircleImageView = itemView.article_image

    init {
        itemView.setOnClickListener(this)
    }

    fun setItemClickListener(itemClickListener : ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onClick(p0 : View?) {
        itemClickListener.onClick(p0!!, adapterPosition)
    }

}