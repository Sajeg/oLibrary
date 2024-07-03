package com.sajeg.olibrary.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.sajeg.olibrary.MainActivity
import com.sajeg.olibrary.R
import com.sajeg.olibrary.ui.theme.OLibraryTheme

class Account : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OLibraryTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = true,
                            onClick = { switchToMain() },
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.home),
                                    contentDescription = ""
                                )
                            }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { activateQRCode() },
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.qrcode),
                                    contentDescription = ""
                                )
                            }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { /*TODO*/ },
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.person),
                                    contentDescription = ""
                                )
                            }
                        )
                    }
                }
                ) { innerPadding ->
                    var modifier = Modifier.padding(innerPadding)
                    val webView = WebView(this)
                    webView.settings.apply {
                        domStorageEnabled = true
                        loadsImagesAutomatically = true
                        javaScriptEnabled = true
                    }
                    webView.webViewClient = WebViewClient()
                    webView.loadUrl("https://www.stadtbibliothek.oldenburg.de/protected/my-account/overview")
                }
            }
        }
    }

    private fun activateQRCode() {
        startActivity(Intent(this, com.sajeg.olibrary.qrcodescanner.Activity::class.java))
    }

    private fun switchToMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
