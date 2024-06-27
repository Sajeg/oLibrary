package com.sajeg.olibrary

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.bumptech.glide.Glide
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

class GradientImagePainter(
    private val imagePainter: Painter
) : Painter() {

    override val intrinsicSize: androidx.compose.ui.geometry.Size
        get() = imagePainter.intrinsicSize

    override fun DrawScope.onDraw() {
        drawIntoCanvas { canvas ->
            with(imagePainter) {
                draw(size, alpha = 1.0f)
            }

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White
                    )
                ),
                size = size
            )
        }
    }
}

@Composable
fun DisplayBookInfo(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
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
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
//                val newBitmap = Bitmap.createBitmap(
//                    bitmap.value!!.asImageBitmap().width,
//                    bitmap.value!!.asImageBitmap().height,
//                    bitmap.value!!.config
//                )
//                val canvas = android.graphics.Canvas(newBitmap)
//                canvas.drawBitmap(bitmap.value!!, 0f, 0f, Paint())

                Image(
                    painter = GradientImagePainter(BitmapPainter(bitmap.value!!.asImageBitmap())),
//                    bitmap = bitmap.value!!.asImageBitmap(),
                    contentDescription = "Cover",
                    contentScale = ContentScale.FillWidth,
                )
            }
        } else {
            // Display a loading indicator or placeholder image
            // For example:
            Text("Loading Image...")
        }
    }
}
