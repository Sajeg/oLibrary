package com.sajeg.olibrary

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.HttpURLConnection
import java.net.URL

object WebsiteFetcher {
    private lateinit var lastDocument: Document

    suspend fun fetchWebsiteContent(url: String): List<Element> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("WebsiteFetcher", "Fetching content from: $url")
                val websiteUrl = URL(url)
                val connection = websiteUrl.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true

                val inputStream = connection.inputStream


                lastDocument = Jsoup.parse(inputStream.bufferedReader().use { it.readText() })
                val title: List<Element> = lastDocument.select("div.arena-record-title a span").toList()
                Log.d("Title", title.toString())
                return@withContext title
            } catch (e: Exception) {
                Log.e("WebsiteFetcher", "Error fetching content: $e")
                return@withContext listOf()
            }
        }
    }
}