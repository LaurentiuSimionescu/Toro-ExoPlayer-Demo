package com.exo

import android.graphics.Color
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import im.ene.toro.ToroPlayer
import im.ene.toro.ToroUtil
import im.ene.toro.exoplayer.ExoPlayerViewHelper
import im.ene.toro.exoplayer.exo.VideoSource
import im.ene.toro.media.PlaybackInfo
import im.ene.toro.widget.Container

class VideoFeedViewHolder(private val view: View) : RecyclerView.ViewHolder(view), ToroPlayer {

    private val coverImageView: ImageView = view.findViewById(R.id.videoCoverImageView)

    private val playerView: PlayerView = view.findViewById(R.id.playerView)

    val startTimeDelay: TextView = view.findViewById(R.id.startTimeDelay)

    var exoPlayerViewHelper: ExoPlayerViewHelper? = null

    var startTime: Long = 0

    init {
        playerView.useController = false
        playerView.resizeMode =
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM //This will increase video's height or width to fit with maintaining aspect ratios of video.

    }

    var uri: Uri? = null

    fun onBind(item: VideoSource) {
        showCover(true)
        Glide.with(view.context).load(item.preview()).centerCrop().into(coverImageView)
        uri = item.uri()
    }

    fun showCover(show: Boolean) {
        if (show) {
            coverImageView.visibility = View.VISIBLE
        } else {
            coverImageView.visibility = View.INVISIBLE
        }
    }

    override fun isPlaying() = exoPlayerViewHelper?.isPlaying ?: false

    override fun getPlayerView() = playerView

    override fun pause() {
        exoPlayerViewHelper?.pause()
        showCover(true)
    }

    override fun wantsToPlay() = ToroUtil.visibleAreaOffset(this, itemView.parent) >= 0.85

    override fun play() {
        startTime = System.currentTimeMillis()
        startTimeDelay.text = ""
        showCover(true)
        exoPlayerViewHelper?.play()
    }

    override fun getCurrentPlaybackInfo() = PlaybackInfo()

    override fun release() {
        exoPlayerViewHelper?.release()
        exoPlayerViewHelper = null
    }

    override fun initialize(container: Container, playbackInfo: PlaybackInfo) {
        if (exoPlayerViewHelper == null) {
            uri?.let {
                exoPlayerViewHelper = ExoPlayerViewHelper(this, it)
            }
        }
        exoPlayerViewHelper?.addPlayerEventListener(object : ToroPlayer.EventListener {
            override fun onBuffering() {
                showCover(true)
            }

            override fun onFirstFrameRendered() {
                showCover(false)
                startTimeDelay.text = (System.currentTimeMillis() - startTime).toString()
                startTimeDelay.setTextColor(Color.RED)

            }

            override fun onPlaying() {

            }

            override fun onPaused() {
                showCover(true)
            }

            override fun onCompleted() {
                showCover(true)
                playerView.player?.seekTo(0)
                exoPlayerViewHelper?.play()
            }

        })
        exoPlayerViewHelper?.initialize(container, playbackInfo)
    }

    override fun getPlayerOrder() = adapterPosition

}