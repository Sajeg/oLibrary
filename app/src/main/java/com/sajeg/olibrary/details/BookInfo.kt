package com.sajeg.olibrary.details

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sajeg.olibrary.MainActivity
import com.sajeg.olibrary.R
import com.sajeg.olibrary.ui.theme.OLibraryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

var background: Color = Color.Transparent
var width: Dp = 0F.dp
var height: Dp = 0F.dp

class BookInfo : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BookData.addBookData(
            intent.getIntExtra("recordId", -1),
            intent.getStringExtra("title")!!,
            intent.getStringExtra("author")!!,
            intent.getStringExtra("year")!!,
            intent.getStringExtra("language")!!,
            intent.getStringExtra("genre")!!,
            intent.getStringExtra("series")!!,
            intent.getStringExtra("imageLink")!!,
            intent.getStringExtra("url")!!
        )
        enableEdgeToEdge()
        setContent {
            OLibraryTheme {
                LaunchedEffect(key1 = BookData.desc) {
                    withContext(Dispatchers.IO) {
                        BookData.fetchBookData()
                    }
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(text = BookData.getCurrentBook().title) },
                            navigationIcon = {
                                IconButton(
                                    onClick = { backToMainActivity() },
                                    content = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.back),
                                            contentDescription = "Go Back"
                                        )
                                    })
                            }
                        )
                    }
                ) { innerPadding ->
                    val contentModifier = Modifier
                        .padding(innerPadding)
                    DisplayBookInfo(contentModifier)
                }
            }
        }
    }

    private fun backToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}

@Composable
fun DisplayBookCover(modifier: Modifier) {
    val configuration = LocalConfiguration.current
    height = configuration.screenHeightDp.dp
    width = configuration.screenWidthDp.dp
    background = MaterialTheme.colorScheme.background
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    val glideImage =
        Glide.with(LocalContext.current).asBitmap().load(BookData.getCurrentBook().imgUrl)
    LaunchedEffect(key1 = BookData.getCurrentBook().imgUrl) {
        withContext(Dispatchers.IO) {
            val futureTarget = glideImage.submit()
            bitmap.value = futureTarget.get()
            futureTarget.cancel(false)
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (bitmap.value != null) {
            Image(
                bitmap = bitmap.value!!.asImageBitmap(),
                contentDescription = "Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.padding(15.dp)
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DisplayBookInfo(modifier: Modifier) {
    Column {
        GlideImage(
            model = BookData.getCurrentBook().imgUrl,
            contentDescription = "Das Cover",
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp),
        )
    }
    Column(
        modifier.fillMaxSize().padding(20.dp),
    ) {
        Row {
            GlideImage(
                model = BookData.getCurrentBook().imgUrl,
                contentDescription = "Das Cover",
            )
            Column (
                modifier = Modifier.padding(start = 20.dp)
            ){
                Text(text = "Autor*in: ${BookData.getCurrentBook().getAuthorFormated(true)}")
                Text(text = "Jahr: ${BookData.getCurrentBook().year}")
                Text(text = "Sprache: ${BookData.getCurrentBook().language}")
                Text(text = "Reihe: ${BookData.getCurrentBook().series}")
                Text(text = "Genre: ${BookData.getCurrentBook().genre}")
                Text(text = "ISBN: ${BookData.isbn}")
            }
        }
        Column {
            Text(text = BookData.desc)
        }
    }
}
