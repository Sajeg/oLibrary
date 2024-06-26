package com.sajeg.olibrary

import android.os.Bundle
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.sajeg.olibrary.ui.theme.OLibraryTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.nodes.Element

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCompose(modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(false) }
//        var result by remember { mutableStateListOf<Element>() }
    val result: MutableState<MutableList<Element>> =
        remember { mutableStateOf(mutableStateListOf<Element>()) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        SearchBar(
            query = searchQuery,
            onQueryChange = {
                searchQuery = it
                CoroutineScope(Dispatchers.IO).launch {
                    result.value = WebsiteFetcher.fetchWebsiteContent(
                        "https://www.stadtbibliothek.oldenburg.de" +
                                "/olsuchergebnisse?p_r_p_arena_urn%3Aarena_" +
                                "search_query=${searchQuery.replace(" ", "+")}"
                    ).toMutableList()
                }
            },
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
                for (book in result.value) {
                    Text(text = book.text())
                }
            }
        )
    }
}

