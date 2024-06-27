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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sajeg.olibrary.ui.theme.OLibraryTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun MainCompose(modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(false) }
//        var result by remember { mutableStateListOf<Element>() }
    val result: MutableState<MutableList<Book>> =
        remember { mutableStateOf(mutableStateListOf<Book>()) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        SearchBar(
            query = searchQuery,
            onQueryChange = {
                searchQuery = it
                CoroutineScope(Dispatchers.IO).launch {
                    result.value = WebsiteFetcher.searchBooks(
                        searchQuery
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
                if (result.value.isNotEmpty()) {

                    LazyColumn {
                        for (book in result.value) {
                            item {
                                ListItem(
                                    headlineContent = { Text(text = book.title) },
                                    leadingContent = {
                                        GlideImage(
                                            model = book.imageLink,
                                            contentDescription = "The Book Cover",
                                            modifier = Modifier.size(60.dp)
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            text = "Von ${book.author} aus dem Jahr ${book.year} " +
                                                    "auf ${book.language} als ${book.genre}"
                                        )
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Row (Modifier.fillMaxWidth()){
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
            }
        )
    }
}

