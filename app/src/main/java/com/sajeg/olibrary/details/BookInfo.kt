package com.sajeg.olibrary.details

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        val sendIntent: Intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                BookData.getCurrentBook().url
                                            )
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            type = "text/plain"
                                        }

                                        val shareIntent =
                                            Intent.createChooser(sendIntent, "Share the Book")
                                        ContextCompat.startActivity(
                                            this@BookInfo,
                                            shareIntent,
                                            null
                                        )
                                    },
                                    content = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.share),
                                            contentDescription = "Share the Book"
                                        )
                                    }
                                )
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

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalLayoutApi::class)
@Composable
fun DisplayBookInfo(modifier: Modifier) {
    val imageHeight = remember { mutableIntStateOf(0) }
    Column {
        GlideImage(
            model = BookData.getCurrentBook().imgUrl,
            contentDescription = "Das Cover",
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp)
                .alpha(0.8f),
        )
    }
    Column(
        modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Row {
            GlideImage(
                model = BookData.getCurrentBook().imgUrl,
                contentDescription = "Das Cover",
                modifier = Modifier.onSizeChanged {
                    imageHeight.intValue = it.height
                }
            )
            FlowColumn(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(start = 20.dp).height(228.dp)
            ) {
                Text(text = "Autor*in: ${BookData.getCurrentBook().getAuthorFormated(true)}", color = MaterialTheme.colorScheme.onBackground)
                if (BookData.getCurrentBook().year != "") {
                    Text(
                        text = "Jahr: ${BookData.getCurrentBook().year}",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (BookData.getCurrentBook().language != "") {
                    Text(
                        text = "Sprache: ${BookData.getCurrentBook().language}",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (BookData.getCurrentBook().series != "") {
                    Text(
                        text = "Reihe: ${BookData.getCurrentBook().series}",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(text = "Genre: ${BookData.getCurrentBook().genre}", color = MaterialTheme.colorScheme.onBackground)
                Text(text = "ISBN: ${BookData.isbn}", color = MaterialTheme.colorScheme.onBackground)
            }
        }
        Column (
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(text = BookData.desc)
        }
    }
}
