package com.example.mykeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button

class MyKeyboardService : InputMethodService() {

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)

        val btnHello = view.findViewById<Button>(R.id.btn_hello)
        val btnSpace = view.findViewById<Button>(R.id.btn_space)
        val btnDel = view.findViewById<Button>(R.id.btn_backspace)

        btnHello.setOnClickListener {
            currentInputConnection?.commitText("Hello World ", 1)
        }

        btnSpace.setOnClickListener {
            currentInputConnection?.commitText(" ", 1)
        }

        btnDel.setOnClickListener {
            currentInputConnection?.deleteSurroundingText(1, 0)
        }

        return view
    }
}
