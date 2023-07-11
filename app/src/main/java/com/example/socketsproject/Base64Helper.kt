package com.example.socketsproject

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

object Base64Helper {

    fun getServerPublicKey(): PublicKey {
        val publicBytes: ByteArray = Base64.decode(Store.serverKey.toByteArray(), Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(publicBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    fun encode(text: ByteArray?): String {
        return Base64.encode(text, Base64.NO_WRAP).decodeToString().replace("\n", "")
    }
}