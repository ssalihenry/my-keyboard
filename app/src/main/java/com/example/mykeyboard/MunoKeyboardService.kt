package com.example.mykeyboard

import android.annotation.SuppressLint
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient

class MunoKeyboardService : InputMethodService() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateInputView(): View {
        // Create the WebView
        val webView = WebView(this)
        
        // Force the WebView to only take up as much space as the buttons need
        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        webView.settings.javaScriptEnabled = true
        webView.setBackgroundColor(0) 
        webView.setWebViewClient(WebViewClient())
        
        webView.addJavascriptInterface(WebAppInterface(this), "Android")
        webView.loadUrl("file:///android_asset/muno_keyboard.html")
        
        return webView
    }

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
