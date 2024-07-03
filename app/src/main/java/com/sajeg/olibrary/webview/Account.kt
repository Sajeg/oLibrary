package com.sajeg.olibrary.webview

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.webkit.WebChromeClient
import android.webkit.WebView
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
import androidx.compose.ui.viewinterop.AndroidView
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
                            selected = false,
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
                            selected = true,
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
                    AndroidView(factory = {
                        WebView(it).apply {
                            settings.javaScriptEnabled = true
                            settings.loadsImagesAutomatically = true
                            settings.domStorageEnabled = true
                            settings.setSupportMultipleWindows(true)
                            webChromeClient = object : WebChromeClient() {
                                override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                                    val newWebView = WebView(view!!.context)
                                    newWebView.settings.javaScriptEnabled = true
                                    newWebView.settings.domStorageEnabled = true
                                    newWebView.settings.setSupportMultipleWindows(true)

                                    val dialog = Dialog(view.context)
                                    dialog.setContentView(newWebView)
                                    dialog.show()

                                    val transport = resultMsg?.obj as WebView.WebViewTransport
                                    transport.webView = newWebView
                                    resultMsg.sendToTarget()
                                    return true
                                }
                            }
                            webViewClient = CustomWebViewClient()
                            loadUrl("https://www.stadtbibliothek.oldenburg.de/protected/my-account/overview")
                        }
                    })
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
