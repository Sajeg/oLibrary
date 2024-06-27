package com.sajeg.olibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sajeg.olibrary.ui.theme.OLibraryTheme

class BookInfo : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OLibraryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BookData.addBookData(
                        intent.getStringExtra("title")!!,
                        intent.getStringExtra("author")!!,
                        intent.getStringExtra("year")!!,
                        intent.getStringExtra("language")!!,
                        intent.getStringExtra("genre")!!,
                        intent.getStringExtra("imageLink")!!,
                        intent.getStringExtra("url")!!
                    )
                    DisplayBookInfo(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DisplayBookInfo(modifier: Modifier) {
    Column (
        modifier = modifier
    ){
        Text(text = BookData.getCurrentBook().title)
    }
}
