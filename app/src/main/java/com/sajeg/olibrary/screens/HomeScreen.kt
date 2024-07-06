package com.sajeg.olibrary.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sajeg.olibrary.Book
import com.sajeg.olibrary.Details
import com.sajeg.olibrary.R
import com.sajeg.olibrary.database.DatabaseBookManager
import com.sajeg.olibrary.db
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalGlideComposeApi::class)
@SuppressLint("MutableCollectionMutableState")

@Composable
fun HomeScreen(navController: NavController) {
    var recommendations by remember { mutableStateOf<List<Book>?>(null) }
    CheckForUpdates(LocalContext.current)
    Column {
        Search(navController = navController)

        if (recommendations == null) {
            LaunchedEffect(recommendations) {
                CoroutineScope(Dispatchers.IO).launch {
                    recommendations = db.bookDao().getRandomBooks(5)
                }
            }
        } else {
            Text(text = "Zufällige Bücher: ", modifier = Modifier.padding(horizontal = 15.dp))
            LazyRow(
                modifier = Modifier.padding(horizontal = 15.dp)
            ) {
                for (book in recommendations!!) {
                    item {
                        GlideImage(
                            model = book.imgUrl,
                            contentDescription = "Recommendation",
                            modifier = Modifier
                                .size(height = 200.dp, width = 150.dp)
                                .clickable {
                                    navController.navigate(Details(book.rowid!!, book.title))
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class
)
fun Search(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Book>>(emptyList()) }
    var isActive by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf("*") }
    var refresh by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            val results = withContext(Dispatchers.IO) {
                db.bookDao().search(searchQuery, filter)
            }
            searchResults = results
            Log.d("Results", searchResults.toString())
        } else {
            searchResults = mutableListOf()
        }
    }
    if (refresh) {
        LaunchedEffect(refresh) {
            coroutineScope.launch {
                searchResults = withContext(Dispatchers.IO) {
                    db.bookDao().search(searchQuery, filter)
                }
                refresh = false
            }
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
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp)
                ) {
                    val count = 5
                    SegmentedButton(
                        selected = filter == "*",
                        onClick = { filter = "*"; refresh = true },
                        shape = SegmentedButtonDefaults.itemShape(0, count)
                    ) {
                        Text(text = "All")
                    }
                    SegmentedButton(
                        selected = filter == "title:",
                        onClick = { filter = "title:"; refresh = true },
                        shape = SegmentedButtonDefaults.itemShape(1, count)
                    ) {
                        Text(text = "Title")
                    }
                    SegmentedButton(
                        selected = filter == "author:",
                        onClick = { filter = "author:"; refresh = true },
                        shape = SegmentedButtonDefaults.itemShape(2, count)
                    ) {
                        Text(text = "Author")
                    }
                    SegmentedButton(
                        selected = filter == "series:",
                        onClick = { filter = "series:"; refresh = true },
                        shape = SegmentedButtonDefaults.itemShape(3, count)
                    ) {
                        Text(text = "Series")
                    }
                    SegmentedButton(
                        selected = filter == "genre:",
                        onClick = { filter = "genre:"; refresh = true },
                        shape = SegmentedButtonDefaults.itemShape(4, count)
                    ) {
                        Text(text = "Genre")
                    }
                }
                LazyColumn {
                    for (book in searchResults) {
                        item {
                            ListItem(
                                modifier = Modifier.clickable {
                                    navController.navigate(Details(book.rowid!!, book.title))
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
                                    var desc = book.getAuthorFormated()
                                    if (book.year != "") {
                                        desc += " aus dem Jahr ${book.year}"
                                    }
                                    if (book.language != "") {
                                        desc += " auf ${book.language}"
                                    }
                                    if (book.series != "") {
                                        desc += " in der Reihe ${book.series}"
                                    }
                                    if (book.genre != "") {
                                        desc += " als ${book.genre}"
                                    }
                                    Text(
                                        text = desc
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