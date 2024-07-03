package com.sajeg.olibrary.details

import android.util.Log
import com.sajeg.olibrary.Book
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.HttpURLConnection
import java.net.URL

class BookData(
    recordID: Int?,
    title: String,
    author: String,
    year: String,
    language: String,
    genre: String,
    series: String?,
    imageLink: String,
    url: String
) : Book(recordID, title, author, year, language, genre, series, imageLink, url) {
    private lateinit var bookDetailsDoc: Document
    var desc: String = ""

    fun fetchBookData() {
        try {
            val websiteUrl = URL(url)
            val connection = websiteUrl.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true

            val inputStream = connection.inputStream

            bookDetailsDoc = Jsoup.parse(inputStream.bufferedReader().use { it.readText() })
            desc = bookDetailsDoc.select("div.arena-detail-description div.arena-value span")
                .lastOrNull()!!.text()
        } catch (e: Exception) {
            Log.e("WebsiteFetcher", "Error fetching content: $e")
        }
    }
}