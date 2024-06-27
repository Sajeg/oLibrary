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

                for (i in 0..<
                        minOf(
                            title.size,
                            author.size,
                            year.size,
                            language.size,
                            genre.size,
                            image.size
                        )) {
                    Log.d("Image", image[i])
                    output.add(
                        Book(
                            title = title[i].text(),
                            author = author[i].text(),
                            year = year[i].text(),
                            language = language[i].text(),
                            genre = genre[i].text(),
                            imageLink = "https://www.stadtbibliothek.oldenburg.de" + image[i]
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
}