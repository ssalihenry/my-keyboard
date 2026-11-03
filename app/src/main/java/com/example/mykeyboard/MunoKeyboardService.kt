package com.example.mykeyboard

import android.annotation.SuppressLint
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient

class MunoKeyboardService : InputMethodService() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateInputView(): View {
        // Create the WebView
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.setBackgroundColor(0) // Makes background transparent if needed
        webView.setWebViewClient(WebViewClient())
        
        // Add the JavaScript Bridge so HTML can talk to Android
        webView.addJavascriptInterface(WebAppInterface(this), "Android")
        
        // Load your HTML file from the assets folder
        webView.loadUrl("file:///android_asset/muno_keyboard.html")
        
        return webView
    }

    // The bridge class that receives commands from your HTML
    inner class WebAppInterface(private val context: Context) {
        
        @JavascriptInterface
        fun sendKey(key: String) {
            currentInputConnection?.commitText(key, 1)
        }

        @JavascriptInterface
        fun deleteKey() {
            currentInputConnection?.deleteSurroundingText(1, 0)
        }
    }
}
