package com.sajeg.olibrary

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.sajeg.olibrary.ui.theme.OLibraryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookInfo : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BookData.addBookData(
            intent.getStringExtra("title")!!,
            intent.getStringExtra("author")!!,
            intent.getStringExtra("year")!!,
            intent.getStringExtra("language")!!,
            intent.getStringExtra("genre")!!,
            intent.getStringExtra("imageLink")!!,
            intent.getStringExtra("url")!!
        )
        enableEdgeToEdge()
        setContent {
            OLibraryTheme {
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
                    DisplayBookInfo(Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun backToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DisplayBookInfo(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
//        GlideImage(
//            model = BookData.getCurrentBook().imageLink,
//            contentDescription = "The Book Cover",
//            modifier = Modifier
//                .fillMaxWidth()
//            //.aspectRatio(0.6f)
//            //.blur(15.dp)
//        )

        val bitmap = remember { mutableStateOf<Bitmap?>(null) }
        val glideImage =
            Glide.with(LocalContext.current).asBitmap().load(BookData.getCurrentBook().imageLink)
        LaunchedEffect(key1 = BookData.getCurrentBook().imageLink) {
            withContext(Dispatchers.IO) {
                val futureTarget = glideImage.submit()
                bitmap.value = futureTarget.get()
                futureTarget.cancel(false)
            }
        }
        if (bitmap.value != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                bitmap.value!!.asImageBitmap()
            }
        } else {
            // Display a loading indicator or placeholder image
            // For example:
            Text("Loading Image...")
        }
    }
}
//    LaunchedEffect(key1 = BookData.getCurrentBook().imageLink) {
//        Glide.with(LocalContext.current)
//            .asBitmap()
//            .load(BookData.getCurrentBook().imageLink)
//            .submit()
//            .onSuccess { loadedBitmap ->
//                bitmap.value = loadedBitmap
//            }
//    }
//
//    Canvas(modifier = Modifier.fillMaxSize()) {
//        bitmap.value?.let {
//            drawImage(ImageBitmap(it))
//
//            val path = androidx.compose.ui.graphics.Path().apply {
//                // Define your custom path here
//            }
//            drawPath(
//                path = path, brush = Brush.linearGradient(
//                    colors = listOf(Color.Red, Color.Green),
//                    start = Offset(0f, 0f),
//                    end = Offset(size.width, size.height)
//                )
//            )
//        }
//    }
