package com.sajeg.olibrary.details

import android.util.Log
import com.sajeg.olibrary.Book
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.HttpURLConnection
import java.net.URL

object BookData {
    private lateinit var currentBook: Book
    private lateinit var bookDetailsDoc: Document

    fun getCurrentBook(): Book {
        return currentBook
    }

    fun getDesc(): String? {
        return try {
            bookDetailsDoc.select("div.arena-detail-description div.arena-value span")
                .lastOrNull()!!.text()
        } catch (e:Exception) { null }
    }

    fun addBookData(
        recordID: Int,
        title: String,
        author: String,
        year: String,
        language: String,
        genre: String,
        series: String,
        imageLink: String,
        url: String
    ) {
        currentBook = Book(
            recordId = recordID,
            title = title,
            author = author,
            year = year,
            language = language,
            genre = genre,
            series = series,
            imgUrl = imageLink,
            url = url
        )

        try {
            val websiteUrl = URL(url)
            val connection = websiteUrl.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true

            val inputStream = connection.inputStream

            bookDetailsDoc = Jsoup.parse(inputStream.bufferedReader().use { it.readText() })
        } catch (e: Exception) {
            Log.e("WebsiteFetcher", "Error fetching content: $e")
        }
    }
}