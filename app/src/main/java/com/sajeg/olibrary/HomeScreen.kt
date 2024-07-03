package com.sajeg.olibrary

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@SuppressLint("MutableCollectionMutableState")
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class
)
@Composable
fun HomeScreen(navController: NavController) {
    //val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults = mutableListOf<Book>()
    var isActive by remember { mutableStateOf(false) }


    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        SearchBar(
            query = " ", //searchQuery,
            onQueryChange = {
//                viewModel.setSearchQuery(it)
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
                                //navController.navigate()
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