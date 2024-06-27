package com.sajeg.olibrary

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.HttpURLConnection
import java.net.URL

object BookData {
    private lateinit var currentBook: Book
    private lateinit var bookDetailsDoc: Document

    fun getCurrentBook(): Book{
        return currentBook
    }

    fun addBookData(
        title: String,
        author: String,
        year: String,
        language: String,
        genre: String,
        imageLink: String,
        url: String
    ) {
        currentBook = Book(
            title = title,
            author = author,
            year = year,
            language = language,
            genre = genre,
            imageLink = imageLink,
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