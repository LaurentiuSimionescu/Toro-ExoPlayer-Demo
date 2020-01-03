package im.ene.toro.exoplayer.exo

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.offline.DownloadService

class MediaCachingHelper(private val downloadTracker: NewDownloadTracker) {

    fun init(context: Context) {
        try {
            DownloadService.start(context, DemoDownloadService::class.java)
        } catch (exception: IllegalStateException) {
            DownloadService.startForeground(context, DemoDownloadService::class.java)
        }
    }

    fun cache(list: ArrayList<Uri>) {
        list.forEach { item ->
            downloadTracker.download(item)
        }
    }

    fun cacheVideoSources(list: ArrayList<VideoSource>) {
        list.forEach { item ->
            downloadTracker.download(item.uri())
        }
    }

    fun remove(list: ArrayList<Uri>) {
        list.forEach { item -> downloadTracker.remove(item) }
    }

    fun removeVideoSources(list: ArrayList<VideoSource>) {
        list.forEach { item -> downloadTracker.remove(item.uri()) }
    }

}

/*
*

ExoPlayerApplication application = (ExoPlayerApplication) getApplication();
downloadTracker = application.getDownloadTracker();

// Start the download service if it should be running but it's not currently.
// Starting the service in the foreground causes notification flicker if there is no scheduled
// action. Starting it in the background throws an exception if the app is in the background too
// (e.g. if device screen is locked).
try {
    DownloadService.start(this, DemoDownloadService.class);
} catch (IllegalStateException e) {
    DownloadService.startForeground(this, DemoDownloadService.class);
}

private DownloadTracker downloadTracker;

UriSample uriSample = (UriSample) sample;
downloadTracker.toggleDownload(this, sample.name, uriSample.uri, uriSample.extension);

downloadTracker.isDownloaded(((UriSample) sample).uri);

@Override
public void onDownloadsChanged() {

}

@Override
public void onStart() {
    super.onStart();
    downloadTracker.addListener(this);
}

@Override
public void onStop() {
    downloadTracker.removeListener(this);
    super.onStop();
}

*/
