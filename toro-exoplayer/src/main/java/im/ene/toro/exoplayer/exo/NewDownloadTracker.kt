package im.ene.toro.exoplayer.exo

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.offline.*
import com.google.android.exoplayer2.source.dash.offline.DashDownloadHelper
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadHelper
import com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloadHelper
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import com.google.android.exoplayer2.ui.TrackNameProvider
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Util
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

class NewDownloadTracker(
    private val applicationContext: Context,
    private val dataSourceFactory: DataSource.Factory,
    file: File,
    deserializers: Array<DownloadAction.Deserializer>?
) : DownloadManager.Listener {
    private val trackNameProvider: TrackNameProvider
    private val listeners: CopyOnWriteArraySet<Listener>
    private val trackedDownloadStates: HashMap<Uri, DownloadAction>
    private val actionFile: ActionFile = ActionFile(file)
    private val actionFileWriteHandler: Handler

    init {
        trackNameProvider = DefaultTrackNameProvider(applicationContext.resources)
        listeners = CopyOnWriteArraySet()
        trackedDownloadStates = HashMap()

        val actionFileWriteThread = HandlerThread("DownloadTracker")
        actionFileWriteThread.start()
        actionFileWriteHandler = Handler(actionFileWriteThread.looper)
        loadTrackedActions(if (deserializers != null && deserializers.isNotEmpty()) deserializers else DownloadAction.getDefaultDeserializers())
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun download(uri: Uri) {
        if (!isDownloaded(uri)) {
            val downloadHelperImpl = DownloadHelperImpl(trackNameProvider, getDownloadHelper(uri, null), uri)
            downloadHelperImpl.prepare(object : DownloadHelperImpl.PrepareListener {
                override fun onPrepared(downloadAction: DownloadAction?) {
                    if (downloadAction != null) {
                        internalDownload(downloadAction)
                    } else {
                        // something went wrong
                    }
                }
            })
        }
    }

    fun remove(uri: Uri) {
        if (isDownloaded(uri)) {
            val removeAction = getDownloadHelper(
                uri,
                null
            ).getRemoveAction(Util.getUtf8Bytes(uri.toString())) // maybe replace with unique id
            startServiceWithAction(removeAction)
        }
    }

    fun getOfflineStreamKeys(uri: Uri): List<StreamKey> {
        if (!trackedDownloadStates.containsKey(uri)) {
            return Collections.emptyList()
        }
        return trackedDownloadStates[uri]?.getKeys() ?: Collections.emptyList()
    }

    override fun onInitialized(downloadManager: DownloadManager?) {
        // do nothing
    }

    override fun onTaskStateChanged(downloadManager: DownloadManager?, trackState: DownloadManager.TaskState?) {
        if (trackState?.action == null) {
            Log.d("PreCaching", "trackState is null or trackState.action is null")
            return
        }
        val action = trackState.action
        val uri = action.uri
        if (action.isRemoveAction && trackState.state == DownloadManager.TaskState.STATE_COMPLETED || !action.isRemoveAction && trackState.state == DownloadManager.TaskState.STATE_FAILED) {
            // A download has been removed, or has failed. Stop tracking it.
            if (trackedDownloadStates.remove(uri) != null) {
                handleTrackedDownloadStatesChanged()
            }
        }
    }

    override fun onIdle(downloadManager: DownloadManager?) {
        //do nothing
    }

    private fun loadTrackedActions(deserializers: Array<DownloadAction.Deserializer>) {
        try {
            val allActions = actionFile.load(*deserializers)
            for (action in allActions) {
                trackedDownloadStates[action.uri] = action
            }
        } catch (exception: IOException) {
            Log.d("PreCaching", "Failed to load tracked actions", exception)
        }
    }

    private fun isDownloaded(uri: Uri): Boolean {
        return trackedDownloadStates.containsKey(uri)
    }

    private fun getDownloadHelper(uri: Uri, extension: String?): DownloadHelper {
        val type = Util.inferContentType(uri, extension)
        return when (type) {
            C.TYPE_DASH -> DashDownloadHelper(uri, dataSourceFactory)
            C.TYPE_SS -> SsDownloadHelper(uri, dataSourceFactory)
            C.TYPE_HLS -> HlsDownloadHelper(uri, dataSourceFactory)
            C.TYPE_OTHER -> ProgressiveDownloadHelper(uri)
            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }

    private fun internalDownload(action: DownloadAction) {
        if (trackedDownloadStates.containsKey(action.uri)) {
            // This content is already being downloaded. Do nothing.
            return
        }
        trackedDownloadStates[action.uri] = action
        handleTrackedDownloadStatesChanged()
        startServiceWithAction(action)
    }

    private fun handleTrackedDownloadStatesChanged() {
        for (listener in listeners) {
            listener.onDownloadsChanged()
        }
        val actions = trackedDownloadStates.values.toTypedArray()
        actionFileWriteHandler.post {
            try {
                actionFile.store(*actions)
            } catch (e: IOException) {
                Log.d("PreCaching", "Failed to store tracked actions", e)
            }
        }
    }

    private fun startServiceWithAction(action: DownloadAction) {
        DownloadService.startWithAction(applicationContext, DemoDownloadService::class.java, action, false)
    }

    /**
     * Listens for changes in the tracked downloads.
     */
    interface Listener {

        /**
         * Called when the tracked downloads changed.
         */
        fun onDownloadsChanged()

        fun onDownloadedUri(uri: Uri)

        fun onRemovedUri(uri: Uri)

    }

}
