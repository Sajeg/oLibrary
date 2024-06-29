package com.sajeg.olibrary

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.Glide
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
                    LazyColumn {
                        item {
                            DisplayBookCover(Modifier.padding(innerPadding))
                            DisplayBookInfo(Modifier.padding(innerPadding))
                        }
                    }
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
                draw(Size(width.value, height.value), alpha = 1.0f)
            }

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        background
                    ),
                    endY = width.value
                ),
                size = Size(width.value, height.value)
            )
        }
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
            .drawBehind {
                if (bitmap.value != null) {
                    drawIntoCanvas {
                        with(GradientImagePainter(BitmapPainter(bitmap.value!!.asImageBitmap()))) {
                            draw(Size(width.value, height.value))
                        }
                    }
                }
            }
    ) {
//        if (bitmap.value != null) {
//            Row(
//                horizontalArrangement = Arrangement.Center
//            ) {
//                Image(
//                    painter = GradientImagePainter(BitmapPainter(bitmap.value!!.asImageBitmap())),
////                    bitmap = bitmap.value!!.asImageBitmap(),
//                    contentDescription = "Cover",
//                    modifier = Modifier.size(width + 170.dp),
////                    contentScale = ContentScale.FillWidth,
//                )
//            }
//        } else {
//            Text("Loading Image...")
//        }
    }
}

@Composable
fun DisplayBookInfo(modifier: Modifier) {
    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = BookData.getCurrentBook().title, style = MaterialTheme.typography.displayLarge)
        Text(text = "Von ${BookData.getCurrentBook().author}", style = MaterialTheme.typography.titleMedium)
        Row (horizontalArrangement = Arrangement.Start){
            Text(text = "Bla Bla Description", fontSize = 20.sp)
            Text(text = "Bla Bla Data", fontSize = 20.sp)
        }
    }
}
