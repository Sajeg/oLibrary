package com.sajeg.olibrary.details

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sajeg.olibrary.db
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalLayoutApi::class)
@Composable
fun DisplayBookInfo(recordId: Int) {
    val book = db.bookDao().getById(recordId)!!

    val imageHeight = remember { mutableIntStateOf(0) }
    val glideImage =
        Glide.with(LocalContext.current).asBitmap().load(book.imgUrl)
    LaunchedEffect(key1 = book.imgUrl) {
        withContext(Dispatchers.IO) {
            val futureTarget = glideImage.submit()
            Log.d("ImageFutur", futureTarget.get().height.toString())
            imageHeight.intValue = futureTarget.get().height
            futureTarget.cancel(false)
        }
    }
    Log.d("ImageHeight", imageHeight.toString())
    Column {
        GlideImage(
            model = book.imgUrl,
            contentDescription = "Das Cover",
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp)
                .alpha(0.8f),
        )
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Row {
            GlideImage(
                model = book.imgUrl,
                contentDescription = "Das Cover",
            )
            FlowColumn(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(start = 20.dp)
                //.height(228.dp)
            ) {
                if (book.year.isNotBlank()) {
                    Text(
                        text = "Autor*in: ${book.getAuthorFormated(true)}",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (book.year.isNotBlank()) {
                    Text(
                        text = "Jahr: ${book.year}",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (book.language.isNotBlank()) {
                    Text(
                        text = "Sprache: ${book.language}",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (book.series!!.isNotBlank()) {
                    Text(
                        text = "Reihe: ${book.series}",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (book.genre.isNotBlank()) {
                    Text(
                        text = "Genre: ${book.genre}",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        Column(
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(text = getDesc(book.url))
        }
    }
}

fun getDesc(url: String) {

}