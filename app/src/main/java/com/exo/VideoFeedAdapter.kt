package com.exo

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import im.ene.toro.exoplayer.exo.VideoSource

class VideoFeedAdapter : RecyclerView.Adapter<VideoFeedViewHolder>() {

    private var itemList: ArrayList<VideoSource> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoFeedViewHolder {
        return VideoFeedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_video_feed, parent, false))
    }

    override fun getItemCount(): Int = itemList.count()

    override fun onBindViewHolder(holder: VideoFeedViewHolder, position: Int) {
        holder.onBind(itemList[position])
    }

    fun setItems(items: ArrayList<VideoSource>) {
        this.itemList = items
    }

    fun addItems(items: ArrayList<VideoSource>) {
        this.itemList.addAll(items)
    }

}