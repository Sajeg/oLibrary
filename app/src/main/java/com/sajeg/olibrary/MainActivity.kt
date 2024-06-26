package com.sajeg.olibrary

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.sajeg.olibrary.ui.theme.OLibraryTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.Serializable
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
                }
            }
        }
    }
    private suspend fun fetchWebsiteContent(url: String): Serializable {
        return CoroutineScope(Dispatchers.IO).async {
            try {
                Log.d("WebsiteFetcher", "Fetching content from: $url")
                val websiteUrl = URL(url)
                val connection = websiteUrl.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                Log.d("WebsiteFetcher", "Could establish connection")
                // Get the response headers
//                val headers = connection.headerFields
//                headers.forEach { (key, value) ->
//                    Log.d("WebsiteFetcher", "$key: $value")
//                }

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
                val priceElement = doc.select("div.arena-record-title a span")
                return@async priceElement.text()
            } catch (e: Exception) {
                Log.e("WebsiteFetcher", "Error fetching content: $e")
            }
        }.await()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainCompose(modifier: Modifier = Modifier){
        var searchQuery by remember { mutableStateOf("") }
        var isActive by remember { mutableStateOf(false) }
        Row (
            modifier= Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {},
                active = isActive,
                onActiveChange = { isActive = it },
                placeholder = { Text(text = "Suche nach einem Buch") },
                leadingIcon = {
                    IconButton(onClick = { isActive = !isActive }) {
                        Icon(
                            painter = painterResource(id = if (isActive) R.drawable.back else R.drawable.search),
                            contentDescription = "Search"
                        )
                    }
                },
                trailingIcon = {},
                content = {
                    var state by remember { mutableStateOf("") }
                    LaunchedEffect(key1 = state) {
                        state =  fetchWebsiteContent(
                            "https://www.stadtbibliothek.oldenburg.de" +
                                    "/olsuchergebnisse?p_r_p_arena_urn%3Aarena_" +
                                    "search_query=${searchQuery.replace(" ", "+")}"
                        ).toString()
                    }
                    Text(text = state)
                }
            )
        }
    }
}

