package com.sajeg.olibrary

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.JsonReader
import android.util.Log
import androidx.room.Room
import com.sajeg.olibrary.database.AppDatabase
import com.sajeg.olibrary.database.BookDBItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Document
import java.io.InputStreamReader

object WebsiteFetcher {
    private lateinit var lastDocument: Document
    private lateinit var db: AppDatabase

    // This is deprecated
//    suspend fun searchBooks(query: String): List<Book> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val output = mutableListOf<Book>()
//                val websiteUrl = URL(
//                    "https://www.stadtbibliothek.oldenburg.de" +
//                            "/olsuchergebnisse?p_r_p_arena_urn%3Aarena_" +
//                            "search_query=${query.replace(" ", "+")}"
//                )
//                val connection = websiteUrl.openConnection() as HttpURLConnection
//                connection.instanceFollowRedirects = true
//
//                val inputStream = connection.inputStream
//
//
//                lastDocument = Jsoup.parse(inputStream.bufferedReader().use { it.readText() })
//                val title: List<Element> =
//                    lastDocument.select("div.arena-record-title a span").toList()
//                val author: List<Element> =
//                    lastDocument.select("div.arena-record-author span.arena-value").toList()
//                val year: List<Element> =
//                    lastDocument.select("div.arena-record-year span.arena-value").toList()
//                val language: List<Element> =
//                    lastDocument.select("div.arena-record-language span.arena-value span span")
//                        .toList()
//                val genre: List<Element> =
//                    lastDocument.select("div.arena-record-genre span.arena-value").toList()
//                val image: List<String> =
//                    lastDocument.select("div.arena-book-jacket a img").map { it.attr("src") }
//                        .toList()
//                val url: List<String> =
//                    lastDocument.select("div.arena-record-title a").map { it.attr("href") }
//                        .toList()
//
//                for (i in 0..<
//                        minOf(
//                            title.size,
//                            author.size,
//                            year.size,
//                            language.size,
//                            genre.size,
//                            image.size,
//                            url.size
//                        )) {
//                    Log.d("Image", image[i])
//                    output.add(
//                        Book(
//                            title = title[i].text(),
//                            author = author[i].text(),
//                            year = year[i].text(),
//                            language = language[i].text(),
//                            genre = genre[i].text(),
//                            imageLink = "https://www.stadtbibliothek.oldenburg.de" + image[i],
//                            url = url[i]
//                        )
//                    )
//                }
//
//                return@withContext output
//            } catch (e: Exception) {
//                Log.e("WebsiteFetcher", "Error fetching content: $e")
//                return@withContext listOf()
//            }
//        }
//    }

    fun startDBDownload(context: Context) {
        Log.d("DownloadManager", "Started Download")
        val request =
            DownloadManager.Request(Uri.parse("https://github.com/Sajeg/olibrary-db-updater/raw/master/data.json"))
        request.setTitle("Updating the Database")
        request.setDescription("This is to make sure you have the newest books")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

    }

    suspend fun importBooks(context: Context, fileUri: Uri) = withContext(Dispatchers.IO) {
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "library"
        ).build()
        val bookDao = db.bookDao()
        Log.d("Import", "Starting Import")
        context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
            JsonReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                reader.beginObject()
                var lastUpdate = ""
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "last_update" -> lastUpdate = reader.nextString()
                        "books" -> {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                reader.beginObject()
                                var recordId: Int = -1
                                var title = ""
                                val author = mutableListOf<String>()
                                var year = ""
                                var language = ""
                                var genre = ""
                                var series = ""
                                var imgUrl = ""
                                var url = ""

                                while (reader.hasNext()) {
                                    when (reader.nextName()) {
                                        "recordId" -> recordId = reader.nextString().toInt()
                                        "title" -> title = reader.nextString()
                                        "author" -> {
                                            reader.beginArray()
                                            while (reader.hasNext()) {
                                                author.add(reader.nextString())
                                            }
                                            reader.endArray()
                                        }

                                        "year" -> year = reader.nextString()
                                        "language" -> language = reader.nextString()
                                        "genre" -> genre = reader.nextString()
                                        "series" -> series = reader.nextString()
                                        "imgUrl" -> imgUrl = reader.nextString()
                                        "url" -> url = reader.nextString()
                                        else -> reader.skipValue()
                                    }
                                    if (bookDao.getById(recordId) != null) {
                                        while (reader.hasNext()) {
                                            reader.skipValue()
                                        }
                                    }
                                }
                                reader.endObject()
                                if (bookDao.getById(recordId) == null) {
                                    Log.d("Import", recordId.toString())
                                    bookDao.importBook(
                                        BookDBItem(
                                            recordId,
                                            title,
                                            author.toString(),
                                            year,
                                            language,
                                            genre,
                                            series,
                                            imgUrl,
                                            url
                                        )
                                    )
                                }
                            }
                            reader.endArray()
                        }

                        else -> reader.skipValue()
                    }

                }
                reader.endObject()
                reader.close()
                Log.d("Import", "Completed. Last update: $lastUpdate")
            }
        }
    }

}