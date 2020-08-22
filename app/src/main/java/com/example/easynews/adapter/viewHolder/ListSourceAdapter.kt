package com.example.easynews.adapter.viewHolder

/*

class ListSourceAdapter(private val context: Context, private val webSite : WebSite) : RecyclerView.Adapter<ListSourceViewHolder>() {
    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ListSourceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.source_news, parent, false)

        return ListSourceViewHolder(itemView)
    }

    override fun getItemCount() : Int {
        return webSite.sources!!.size
    }

    override fun onBindViewHolder(holder: ListSourceViewHolder, position: Int) {
        holder.sourceTitle.text = webSite.sources!![position].name
        holder.setItemClickListener(object : ItemClickListener {
            override fun onClick(view : View, position : Int) {
                val intent = Intent(context, ListNews::class.java)
                intent.putExtra("source", webSite.sources!![position].id)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        })
    }
}*/