package com.sajeg.olibrary

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
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
import androidx.room.Room
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sajeg.olibrary.database.AppDatabase
import com.sajeg.olibrary.ui.theme.OLibraryTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private lateinit var downloadReceiver: DownloadReceiver
    private lateinit var db: AppDatabase

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        downloadReceiver = DownloadReceiver()
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "library"
        ).build()
//        Uncomment to start the download and import
        WebsiteFetcher.startDBDownload(this)

        enableEdgeToEdge()
        setContent {
            OLibraryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainCompose(Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
    }

    private fun changeActivity(data: Book) {
        startActivity(Intent(this, BookInfo::class.java).apply {
            putExtra("recordId", data.recordId)
            putExtra("title", data.title)
            putExtra("author", data.author)
            putExtra("year", data.year)
            putExtra("language", data.language)
            putExtra("genre", data.genre)
            putExtra("series", data.series)
            putExtra("imageLink", data.imgUrl)
            putExtra("url", data.url)
        })
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
    @Composable
    fun MainCompose(modifier: Modifier = Modifier) {
        var searchQuery by remember { mutableStateOf("") }
        var isActive by remember { mutableStateOf(false) }
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
                        result.value = db.bookDao().search(
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
                                        modifier = Modifier.clickable {
                                            changeActivity(book)
                                        },
                                        headlineContent = { Text(text = book.title) },
                                        leadingContent = {
                                            GlideImage(
                                                model = book.imgUrl,
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
                        Row(Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                        }
                    }
                }
            )
        }
    }
}
