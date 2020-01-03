package com.exo

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import im.ene.toro.CacheManager
import im.ene.toro.exoplayer.exo.VideoSource
import im.ene.toro.exoplayer.exo.VideoSourceImpl
import kotlinx.android.synthetic.main.activity_player_list.*

class PlayerListActivity : AppCompatActivity() {

    companion object {
        fun getVideoList(): ArrayList<VideoSource> {
            val list = ArrayList<VideoSource>()

            list.add(
                VideoSourceImpl(
                    previewUrl = null, // placeholder image to display if video is loading to slow
                    uri = Uri.parse("https://edge.flowplayer.org/playful.m3u8")
                )
            )

            list.add(
                VideoSourceImpl(
                    previewUrl = null, // placeholder image to display if video is loading to slow
                    uri = Uri.parse("https://mnmedias.api.telequebec.tv/m3u8/29880.m3u8")
                )
            )

            list.add(
                VideoSourceImpl(
                    previewUrl = null, // placeholder image to display if video is loading to slow
                    uri = Uri.parse("https://content.jwplatform.com/manifests/yp34SRmf.m3u8")
                )
            )

            return list;
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_list)

        container.layoutManager = LinearLayoutManager(this)
        container.cacheManager = CacheManager.DEFAULT
        container.adapter = VideoFeedAdapter()

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(container)

        (container.adapter as VideoFeedAdapter).addItems(getVideoList())
        container.adapter?.notifyDataSetChanged()


    }

}