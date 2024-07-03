package com.sajeg.olibrary.webview

import android.os.Handler
import android.os.Looper
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class CustomWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        request?.url?.let { view?.loadUrl(it.toString()) }
        return true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        // Wait a bit before trying to open the login form
        Handler(Looper.getMainLooper()).postDelayed({
            injectLoginScript(view)
        }, 1000) // Wait for 1 second
    }

    private fun injectLoginScript(webView: WebView?) {
        val js = """
            (function() {
                var loginButton = document.querySelector('a.header-shortcuts__account.modal__open[data-modal-class="modal-login"]');
                if (loginButton) {
                    loginButton.click();
                }
            })();
        """
        webView?.evaluateJavascript(js, null)
    }
}