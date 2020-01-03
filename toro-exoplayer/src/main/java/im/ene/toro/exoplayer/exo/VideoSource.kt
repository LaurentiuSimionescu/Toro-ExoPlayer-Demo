package im.ene.toro.exoplayer.exo

import android.net.Uri

interface VideoSource {

    fun uri(): Uri

    fun preview(): String?

}

class VideoSourceImpl(private val previewUrl: String?, private val uri: Uri) :
    VideoSource {

    override fun uri() = uri

    override fun preview() = previewUrl

}