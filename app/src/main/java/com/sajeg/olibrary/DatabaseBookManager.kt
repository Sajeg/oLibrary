package com.sajeg.olibrary

import android.app.DownloadManager
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import android.util.JsonReader
import android.util.Log
import androidx.room.Room
import com.sajeg.olibrary.database.AppDatabase
import com.sajeg.olibrary.database.BookDBItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

object DatabaseBookManager {
    private lateinit var db: AppDatabase

    fun startDBDownload(context: Context, background: Boolean = false) {
        Log.d("DownloadManager", "Started Download")
        val request =
            DownloadManager.Request(Uri.parse("https://github.com/Sajeg/olibrary-db-updater/raw/master/data.json"))
        request.setTitle("Updating the Database")
        request.setDescription("This is to make sure you have the newest books")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        if (background) {
            request.setRequiresDeviceIdle(true)
            request.setRequiresCharging(true)
        }

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
                    }
                    reader.endObject()
                    try {
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
                    } catch (e: Exception) {
                        if (e == SQLiteConstraintException()) {
                            bookDao.updateBook(
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
                }
                reader.endArray()
                reader.close()
                Log.d("Import", "Completed.")
            }
        }
    }

}