package com.example.socketsproject

import android.util.Base64

object Base64Helper {

    fun encode(text: ByteArray?): String {
        return Base64.encode(text, Base64.NO_WRAP).decodeToString().replace("\n", "")
    }
}