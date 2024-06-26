package com.sajeg.olibrary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sajeg.olibrary.database.AppDatabase
import com.sajeg.olibrary.details.BookInfo
import com.sajeg.olibrary.ui.theme.OLibraryTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

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

        enableEdgeToEdge()
        setContent {
            OLibraryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainCompose(Modifier.padding(innerPadding), BookSearchViewModel(db.bookDao()))
                    val connectivityManager = getSystemService(ConnectivityManager::class.java)
                    if (!connectivityManager.isActiveNetworkMetered) {
                        CheckForUpdates()
                    }
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

    @Composable
    fun CheckForUpdates() {
        var needsUpdate by remember { mutableStateOf(false) }
        var installedVersion by remember { mutableStateOf("") }
        var newestVersion by remember { mutableStateOf("") }

        LaunchedEffect(key1 = Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                installedVersion = DatabaseBookManager.installedVersion(this@MainActivity)
            }.join()

            CoroutineScope(Dispatchers.IO).launch {
                newestVersion = DatabaseBookManager.newestVersion(this@MainActivity)
            }.join()

            if (newestVersion != installedVersion) {
                //Open Download Dialog
                needsUpdate = true
            }
        }
        if (needsUpdate) {
            DownloadDialog(context = this@MainActivity, installedVersion, onInput = {
                needsUpdate = false
            })
        }
    }

    @Composable
    fun DownloadDialog(context: Context, installedVersion: String, onInput: () -> Unit) {
        var firstDownload = installedVersion == ""

        AlertDialog(
            onDismissRequest = {
                if (firstDownload) {
                    val activity = (context as? Activity)
                    activity?.finish()
                }
            },
            confirmButton = {
                if (!firstDownload) {
                    TextButton(
                        onClick = {
                            onInput()
                        },
                        content = {
                            Text(text = "Later")
                        }
                    )
                }
                TextButton(
                    onClick = {
                        DatabaseBookManager.startDBDownload(context)
                        firstDownload = false
                        onInput()
                    },
                    content = {
                        Text(text = "Start Download")
                    }
                )
            },
            title = {
                if (firstDownload) {
                    Text(text = "Download Books")
                } else {
                    Text(text = "Update available")
                }
            },
            text = {
                if (firstDownload) {
                    Text(
                        text = "In order to use this App it requires an Download of about 120mb. " +
                                "You can use the App while it downloads the catalog."
                    )
                } else {
                    Text(
                        text = "Update the book catalog now to have the newest titles. " +
                                "You can use the App while it updates the catalog."
                    )
                }
            }
        )
    }

    @SuppressLint("MutableCollectionMutableState")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
    @Composable
    fun MainCompose(modifier: Modifier = Modifier, viewModel: BookSearchViewModel) {
        val searchQuery by viewModel.searchQuery.collectAsState()
        val searchResults by viewModel.searchResults.collectAsState()
        var isActive by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = {
                    viewModel.setSearchQuery(it)
                },
                onSearch = {},
                active = isActive,
                onActiveChange = { isActive = it },
                placeholder = { Text(text = "Search for a book") },
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
                    LazyColumn {
                        items(searchResults) { book ->
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
                                        text = "${book.getAuthorFormated()} aus dem year ${book.year} " +
                                                "auf ${book.language} in der Reihe ${book.series} " +
                                                "als ${book.genre}"
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }
    }
}
