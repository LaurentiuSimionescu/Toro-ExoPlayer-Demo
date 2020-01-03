package im.ene.toro.exoplayer.exo

import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.offline.DownloadAction
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.TrackKey
import com.google.android.exoplayer2.ui.TrackNameProvider
import com.google.android.exoplayer2.util.Util
import java.io.IOException

class DownloadHelperImpl(
    private val trackNameProvider: TrackNameProvider,
    private val downloadHelper: DownloadHelper,
    private val uri: Uri
) {

    /* TrackKey represent sub-playlist, title: 720 × 1280, 1.00 Mbps */
    fun prepare(prepareListener: PrepareListener) {

        val trackKeys = ArrayList<TrackKey>()

        downloadHelper.prepare(object : DownloadHelper.Callback {
            override fun onPrepared(downloadHlp: DownloadHelper?) {
                for (i in 0 until downloadHelper.periodCount) {
                    val trackGroups = downloadHelper.getTrackGroups(i)
                    for (j in 0 until trackGroups.length) {
                        val trackGroup = trackGroups.get(j)
                        for (k in 0 until trackGroup.length) {
                            val trackTitle = trackNameProvider.getTrackName(trackGroup.getFormat(k))
                            val trackKey = TrackKey(i, j, k)

                            trackKeys.add(trackKey)

                            Log.d("PreCaching", "Track Title: $trackTitle")

                        }
                    }
                }

                if (!trackKeys.isEmpty()) {
                    val selectedTrackKeys = ArrayList<TrackKey>()
                    selectedTrackKeys.add(trackKeys[trackKeys.size - 1])
                    val downloadAction =
                        downloadHelper.getDownloadAction(Util.getUtf8Bytes(uri.toString()), selectedTrackKeys)
                    prepareListener.onPrepared(downloadAction)
                } else {
                    Log.d("PreCaching", "No trackKeys found")
                    prepareListener.onPrepared(null)
                }


            }

            override fun onPrepareError(downloadHelper: DownloadHelper?, exception: IOException?) {
                Log.d("PreCaching", "Failed download for uri: $uri")
                prepareListener.onPrepared(null)
            }

        })

    }

    interface PrepareListener {

        fun onPrepared(downloadAction: DownloadAction?)

    }

}
