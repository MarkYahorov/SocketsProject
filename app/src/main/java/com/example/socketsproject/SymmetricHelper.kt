package com.example.socketsproject

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

class SymmetricHelper {

    private val simmetrycCipher = Cipher.getInstance("AES")
    private val simmetrycDecodeCipher = Cipher.getInstance("AES")

    private val simmetrycGenerator = KeyGenerator.getInstance("AES").apply {
        init(256)
    }

    fun createSymmetricKey() {
        Store.simmetrycMyKey = simmetrycGenerator.generateKey()
        simmetrycCipher.init(Cipher.ENCRYPT_MODE, Store.simmetrycMyKey)
    }

    fun enctypt(text: String): ByteArray? {
        return simmetrycCipher.doFinal(text.toByteArray())
    }

    fun decode(text: String): String {
        return simmetrycDecodeCipher.doFinal(
            Base64.decode(
                text, Base64.NO_PADDING
            )
        ).decodeToString()
    }

    fun initChiper(symmetricKey: ByteArray) {
        val serverSimmet =
            SecretKeySpec(symmetricKey, 0, symmetricKey.size, "AES")
        simmetrycDecodeCipher.init(Cipher.DECRYPT_MODE, serverSimmet)
    }
}