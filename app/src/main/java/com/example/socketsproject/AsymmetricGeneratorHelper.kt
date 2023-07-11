package com.example.socketsproject

import android.util.Base64
import java.security.KeyPairGenerator
import javax.crypto.Cipher

class AsymmetricGeneratorHelper {

    private val generator = KeyPairGenerator.getInstance("RSA")
        .apply { initialize(2048) }

    private val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    private val decrCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

    fun createKeys() {
        val pair = generator.genKeyPair()
        Store.myPublicKey = pair.public
        Store.myPrivateKey = pair.private
        Store.myPublicEncoded = Store.myPublicKey.encoded
    }

    fun initCiphers() {
        decrCipher.init(Cipher.DECRYPT_MODE, Store.myPrivateKey)
        cipher.init(Cipher.ENCRYPT_MODE, Base64Helper.getServerPublicKey())
    }

    fun decode(text: String): ByteArray {
        return decrCipher.doFinal(
            Base64.decode(
                text.toByteArray(),
                Base64.NO_PADDING
            )
        )
    }

    fun encode(): ByteArray {
        return cipher.doFinal(Store.simmetrycMyKey.encoded)
    }
}