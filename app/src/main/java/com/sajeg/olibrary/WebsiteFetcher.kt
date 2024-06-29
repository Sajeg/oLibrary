package com.sajeg.olibrary

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.HttpURLConnection
import java.net.URL

object WebsiteFetcher {
    private lateinit var lastDocument: Document

    suspend fun searchBooks(query: String): List<Book> {
        return withContext(Dispatchers.IO) {
            try {
                val output = mutableListOf<Book>()
                val websiteUrl = URL(
                    "https://www.stadtbibliothek.oldenburg.de" +
                            "/olsuchergebnisse?p_r_p_arena_urn%3Aarena_" +
                            "search_query=${query.replace(" ", "+")}"
                )
                val connection = websiteUrl.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true

                val inputStream = connection.inputStream


                lastDocument = Jsoup.parse(inputStream.bufferedReader().use { it.readText() })
                val title: List<Element> =
                    lastDocument.select("div.arena-record-title a span").toList()
                val author: List<Element> =
                    lastDocument.select("div.arena-record-author span.arena-value").toList()
                val year: List<Element> =
                    lastDocument.select("div.arena-record-year span.arena-value").toList()
                val language: List<Element> =
                    lastDocument.select("div.arena-record-language span.arena-value span span")
                        .toList()
                val genre: List<Element> =
                    lastDocument.select("div.arena-record-genre span.arena-value").toList()
                val image: List<String> =
                    lastDocument.select("div.arena-book-jacket a img").map { it.attr("src") }
                        .toList()
                val url: List<String> =
                    lastDocument.select("div.arena-record-title a").map { it.attr("href") }
                        .toList()

                for (i in 0..<
                        minOf(
                            title.size,
                            author.size,
                            year.size,
                            language.size,
                            genre.size,
                            image.size,
                            url.size
                        )) {
                    Log.d("Image", image[i])
                    output.add(
                        Book(
                            title = title[i].text(),
                            author = author[i].text(),
                            year = year[i].text(),
                            language = language[i].text(),
                            genre = genre[i].text(),
                            imageLink = "https://www.stadtbibliothek.oldenburg.de" + image[i],
                            url = url[i]
                        )
                    )
                }

                return@withContext output
            } catch (e: Exception) {
                Log.e("WebsiteFetcher", "Error fetching content: $e")
                return@withContext listOf()
            }
        }
    }

    fun startDBDownload(context: Context) {
        Log.d("DownloadManager", "Started Download")
        val request =
            DownloadManager.Request(Uri.parse("https://github.com/Sajeg/olibrary-db-updater/raw/master/data.json"))
        request.setTitle("Updating the Database")
        request.setDescription("This is to make sure you have the newest books")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

    }

    fun importBooks(){

    }

}

class DownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                val fileUri = cursor.getString(columnIndex)
                // Handle the file URI
                if (fileUri != null) {
                    // For example, show a toast or update the UI
                    Toast.makeText(context, "Download complete: $fileUri", Toast.LENGTH_LONG).show()
                }
            }
            Log.d("DownloadManager", "Finished with id $downloadId")
            cursor.close()
        }
    }
}