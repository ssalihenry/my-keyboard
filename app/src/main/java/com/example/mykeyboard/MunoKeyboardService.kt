package com.example.mykeyboard

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient

class MunoKeyboardService : InputMethodService() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateInputView(): View {
        val webView = WebView(this)
        
        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.setBackgroundColor(0x00000000)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.setWebViewClient(WebViewClient())
        webView.addJavascriptInterface(WebAppInterface(this), "Android")
        webView.loadUrl("file:///android_asset/muno_keyboard.html")
        
        return webView
    }

    inner class WebAppInterface(private val context: Context) {
        
        // 🔥 THIS PLAYS THE KEYBOARD SOUND
        @JavascriptInterface
        fun playClick() {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
        }

        @JavascriptInterface
        fun sendKey(key: String) {
            val ic = currentInputConnection ?: return
            
            when (key) {
                "Backspace" -> {
                    val selectedText = ic.getSelectedText(0)
                    if (selectedText != null && selectedText.length > 0) {
                        ic.commitText("", 1)
                    } else {
                        ic.deleteSurroundingText(1, 0)
                    }
                }
                "enter", "Enter" -> {
                    val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
                    
                    if (action == EditorInfo.IME_ACTION_SEND || action == EditorInfo.IME_ACTION_GO || action == EditorInfo.IME_ACTION_SEARCH) {
                        ic.performEditorAction(action)
                    } else {
                        // 🔥 RAW HARDWARE ENTER (Forces ChatGPT, Chrome, etc. to send)
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                    }
                }
                // Real PC Hardware Commands
                "Esc" -> sendHardwareKey(ic, KeyEvent.KEYCODE_ESCAPE)
                "Tab" -> sendHardwareKey(ic, KeyEvent.KEYCODE_TAB)
                "Caps" -> sendHardwareKey(ic, KeyEvent.KEYCODE_CAPS_LOCK)
                "Ctrl" -> sendHardwareKey(ic, KeyEvent.KEYCODE_CTRL_LEFT)
                "Alt" -> sendHardwareKey(ic, KeyEvent.KEYCODE_ALT_LEFT)
                "F1" -> sendHardwareKey(ic, KeyEvent.KEYCODE_F1)
                "F2" -> sendHardwareKey(ic, KeyEvent.KEYCODE_F2)
                "F3" -> sendHardwareKey(ic, KeyEvent.KEYCODE_F3)
                "F4" -> sendHardwareKey(ic, KeyEvent.KEYCODE_F4)
                "F5" -> sendHardwareKey(ic, KeyEvent.KEYCODE_F5)
                "F6" -> sendHardwareKey(ic, KeyEvent.KEYCODE_F6)
                "F7" -> sendHardwareKey(ic, KeyEvent.KEYCODE_F7)
                "F8" -> sendHardwareKey(ic, KeyEvent.KEYCODE_F8)
                "F9" -> sendHardwareKey(ic, KeyEvent.KEYCODE_F9)
                "F10" -> sendHardwareKey(ic, KeyEvent.KEYCODE_F10)
                "F11" -> sendHardwareKey(ic, KeyEvent.KEYCODE_F11)
                "F12" -> sendHardwareKey(ic, KeyEvent.KEYCODE_F12)
                
                else -> {
                    ic.commitText(key, 1)
                }
            }
        }

        private fun sendHardwareKey(ic: android.view.inputmethod.InputConnection, keyCode: Int) {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
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
