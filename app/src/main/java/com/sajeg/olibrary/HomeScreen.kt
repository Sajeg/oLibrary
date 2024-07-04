package com.sajeg.olibrary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("MutableCollectionMutableState")
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class
)
@Composable
fun HomeScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Book>>(emptyList()) }
    var isActive by remember { mutableStateOf(false) }

    CheckForUpdates(LocalContext.current)

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            val results = withContext(Dispatchers.IO) {
                db.bookDao().search(searchQuery)
            }
            searchResults = results
            Log.d("Results", searchResults.toString())
        } else {
            searchResults = mutableListOf()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .safeDrawingPadding(),
        horizontalArrangement = Arrangement.Center
    ) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
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
                    for (book in searchResults) {
                        item {
                            ListItem(
                                modifier = Modifier.clickable {
                                    navController.navigate(Details(book.rowid!!))
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
            }
        )
    }
}

@Composable
fun CheckForUpdates(context: Context) {
    var needsUpdate by remember { mutableStateOf(false) }
    var installedVersion by remember { mutableStateOf("") }
    var newestVersion by remember { mutableStateOf("") }

    LaunchedEffect(key1 = Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            installedVersion = DatabaseBookManager.installedVersion(context)
        }.join()

        CoroutineScope(Dispatchers.IO).launch {
            newestVersion = DatabaseBookManager.newestVersion(context)
        }.join()

        if (newestVersion != installedVersion) {
            //Open Download Dialog
            needsUpdate = true
        }
    }
    if (needsUpdate) {
        DownloadDialog(context = context, installedVersion, onInput = {
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