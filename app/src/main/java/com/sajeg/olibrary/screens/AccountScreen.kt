package com.sajeg.olibrary.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AccountScreen() {
    AndroidView(factory = {
        WebView(it).apply {
            settings.javaScriptEnabled = true
            settings.loadsImagesAutomatically = true
            settings.domStorageEnabled = true
            settings.setSupportMultipleWindows(true)
            webViewClient = WebViewClient()
            loadUrl("https://www.stadtbibliothek.oldenburg.de/protected/my-account/overview")
        }
    })
}