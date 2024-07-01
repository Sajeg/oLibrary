package com.sajeg.olibrary

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            Log.d("DownloadReceiver", "Download completed. ID: $downloadId")

            if (downloadId != -1L) {
                val downloadManager =
                    context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                try {
                    val uri = downloadManager.getUriForDownloadedFile(downloadId)
                    if (uri != null) {
                        Log.d("DownloadReceiver", "File URI: $uri")
                        CoroutineScope(Dispatchers.IO).launch {
                            DatabaseBookManager.importBooks(context, uri)
                            DatabaseBookManager.setInstalledVersion(
                                context,
                                DatabaseBookManager.newestVersion(context)
                            )
                        }
                    } else {
                        Log.e("DownloadReceiver", "URI is null for download ID: $downloadId")
                    }
                } catch (e: Exception) {
                    Log.e("DownloadReceiver", "Error getting URI: ${e.message}", e)
                }
            }
        }
    }
}