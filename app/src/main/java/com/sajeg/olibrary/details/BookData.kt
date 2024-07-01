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
    var isbn: String = ""
    var desc: String = ""

    fun getCurrentBook(): Book {
        return currentBook
    }

    fun fetchBookData() {
        try {
            val url = getCurrentBook().url
            val websiteUrl = URL(url)
            val connection = websiteUrl.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true

            val inputStream = connection.inputStream

            bookDetailsDoc = Jsoup.parse(inputStream.bufferedReader().use { it.readText() })
            desc = bookDetailsDoc.select("div.arena-detail-description div.arena-value span")
                .lastOrNull()!!.text()
            isbn = bookDetailsDoc.select("div.arena-detail-isbn div.arena-value span")
                .lastOrNull()!!.text()
        } catch (e: Exception) {
            Log.e("WebsiteFetcher", "Error fetching content: $e")
        }
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
    }
}