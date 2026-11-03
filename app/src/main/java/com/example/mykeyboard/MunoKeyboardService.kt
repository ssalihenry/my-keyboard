package com.example.mykeyboard

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient

class MunoKeyboardService : InputMethodService() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateInputView(): View {
        val webView = WebView(this)
        
        // Force it to shrink-wrap to the HTML height
        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Hardware acceleration for smooth typing
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.setBackgroundColor(0x00000000) // Transparent
        
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.setWebViewClient(WebViewClient())
        
        // Add the bridge
        webView.addJavascriptInterface(WebAppInterface(this), "Android")
        
        webView.loadUrl("file:///android_asset/muno_keyboard.html")
        
        return webView
    }

    inner class WebAppInterface(private val context: Context) {
        
        @JavascriptInterface
        fun sendKey(key: String) {
            val inputConnection = currentInputConnection ?: return
            
            if (key == "Backspace") {
                val selectedText = inputConnection.getSelectedText(0)
                if (selectedText != null && selectedText.length > 0) {
                    inputConnection.commitText("", 1)
                } else {
                    inputConnection.deleteSurroundingText(1, 0)
                }
            } else if (key == "enter") {
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            } else {
                inputConnection.commitText(key, 1)
            }
        }

        @JavascriptInterface
        fun hideKeyboard() {
            requestHideSelf(0)
        }

        @JavascriptInterface
        fun copyText() {
            val ic = currentInputConnection ?: return
            val selected = ic.getSelectedText(0)
            if (selected != null && selected.isNotEmpty()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("MUNO", selected))
            }
        }

        @JavascriptInterface
        fun pasteText() {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip() && clipboard.primaryClip != null) {
                val pasteData = clipboard.primaryClip?.getItemAt(0)?.text
                if (pasteData != null) {
                    currentInputConnection?.commitText(pasteData, 1)
                }
            }
        }
    }
}
