package com.example.socketsproject

import android.util.Base64
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream

class AsymmetricGeneratorHelper {

    private val generator = KeyPairGenerator.getInstance("RSA")
        .apply { initialize(2048) }

    private val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    private val decrCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    private var myPublicKey: PublicKey? = null
    private var myPrivateKey: PrivateKey? = null

    fun createKeys() {
        val pair = generator.genKeyPair()
        myPublicKey = pair.public
        myPrivateKey = pair.private

    }

    fun initCiphers(assymetric: String) {
        decrCipher.init(Cipher.DECRYPT_MODE, myPrivateKey)
        cipher.init(Cipher.ENCRYPT_MODE, getServerPublicKey(assymetric))
    }

    fun sendText(out: BufferedWriter, sendText: String) {
        val arraybufferstream = ByteArrayOutputStream()
        CipherOutputStream(arraybufferstream, cipher).apply {
            write(sendText.toByteArray())
            flush()
            close()
        }

        out.write(
            "${
                Base64.encode(arraybufferstream.toByteArray(), Base64.DEFAULT).decodeToString()
                    .replace("\n", "")
            }\r"
        )
        out.flush()
        arraybufferstream.close()
    }


    fun getPublickKey(): ByteArray? {
        return myPublicKey?.encoded
    }

    private fun getServerPublicKey(serverKey: String): PublicKey {
        val publicBytes: ByteArray = Base64.decode(serverKey.toByteArray(), Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(publicBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    fun readText(inputStream: BufferedReader, inputStream1: InputStream): String? {
        val input = inputStream.readLine().replace("\r", "")
        val decodeInput = Base64.decode(input, Base64.NO_PADDING)
        val byteAInput = ByteArrayInputStream(decodeInput)
        val inputCipher = CipherInputStream(byteAInput, decrCipher)
        val baos = ByteArrayOutputStream()

        val b = ByteArray(1024)
        var numberOfBytedRead: Int
        while (inputCipher.read(b).also { numberOfBytedRead = it } >= 0) {
            baos.write(b, 0, numberOfBytedRead)
        }
        baos.close()
        inputCipher.close()
        byteAInput.close()

        return baos.toByteArray().decodeToString()
    }
}