package com.sajeg.olibrary

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sajeg.olibrary.ui.theme.OLibraryTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OLibraryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainCompose(Modifier.padding(innerPadding))
                    fetchWebsiteContent("https://www.stadtbibliothek.oldenburg.de/olsuchergebnisse?p_r_p_arena_urn%3Aarena_search_query=magische")
                }
            }
        }
    }
    private fun fetchWebsiteContent(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("WebsiteFetcher", "Fetching content from: $url")
                val websiteUrl = URL(url)
                val connection = websiteUrl.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                Log.d("WebsiteFetcher", "Could establish connection")
                // Get the response headers
                val headers = connection.headerFields
                headers.forEach { (key, value) ->
                    Log.d("WebsiteFetcher", "$key: $value")
                }

                val inputStream = connection.inputStream
                Log.d("WebsiteFetcher", "Got InputStream")

                // Handle the response based on the content type
                val contentType = connection.contentType
//                Log.d("Result", if (contentType.startsWith("text/html")) {
//                    inputStream.bufferedReader().use { it.readText() }
//                } else {
//                    // Handle different content types if needed
//                    "Unsupported content type: $contentType"
//                })
                val doc: Document = Jsoup.parse(inputStream.bufferedReader().use { it.readText() })
                val priceElement = doc.select("div.arena-record-title a span").firstOrNull()
                if (priceElement != null) {
                    Log.d("Title", priceElement.text())
                }
            } catch (e: Exception) {
                Log.e("WebsiteFetcher", "Error fetching content: $e")
            }
        }
    }
}

@Composable
fun MainCompose(modifier: Modifier = Modifier){

}