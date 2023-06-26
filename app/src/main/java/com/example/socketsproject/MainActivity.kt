package com.example.socketsproject

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var sendBtn: Button
    private lateinit var input: EditText
    private lateinit var textView: TextView
    private lateinit var decr: TextView

    private val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    private val decrCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    private val generator = KeyPairGenerator.getInstance("RSA")
        .apply { initialize(2048) }
    private lateinit var scope: CoroutineScope
    private lateinit var inputStream: BufferedReader
    private lateinit var out: BufferedWriter

    private lateinit var socket: Socket
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        scope = CoroutineScope(Dispatchers.Main)
        scope.launch(Dispatchers.IO) {
            if (!this@MainActivity::socket.isInitialized) {
                try {
                    socket = Socket("192.168.100.9", 33876)
                    val pair = generator.genKeyPair()
                    Store.myPublicKey = pair.public
                    Store.myPrivateKey = pair.private
                    Store.myPublicEncoded = Store.myPublicKey.encoded
                    Log.e("TAG23", Store.myPublicEncoded.toString())
                   out = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                    out.write(
                        "${
                            Base64.encode(Store.myPublicEncoded, Base64.DEFAULT).decodeToString()
                                .replace("\n", "")
                        }\r"
                    )
                    out.flush()
                    inputStream = BufferedReader(InputStreamReader(socket.getInputStream()))
                    Store.serverKey = inputStream.readLine().replace("\r", "")
                    Log.e("TAG23", "serverKey = ${Store.serverKey}")
                    val serverKey = getServerPublicKey()
                    Log.e("TAG23", "normal server key = ${serverKey}")
                    cipher.init(Cipher.ENCRYPT_MODE, serverKey)
                    decrCipher.init(Cipher.DECRYPT_MODE, Store.myPrivateKey)
                } catch (e: Exception) {
                    Log.e("TAG23", "not error ${e.message}")
                }

            }
        }


        sendBtn = findViewById(R.id.send)
        input = findViewById(R.id.editText)
        textView = findViewById(R.id.encrypt_text)
        decr = findViewById(R.id.decrypt_text)

        sendBtn.setOnClickListener {
            scope.launch(Dispatchers.IO) {
                if (socket.isConnected) {
                    Log.e("TAG23", "isConnected")
                    val bytes = input.text.toString().toByteArray()
                    try {
                        val sendText = input.text.toString()
                        val sendEncryptText =
                            Base64.encode(cipher.doFinal(sendText.toByteArray()), Base64.NO_WRAP)
                        out.write("${sendEncryptText.decodeToString()}\r")
                        out.flush()
                        val serverText = inputStream.readLine()
                        Log.e("TAG23", serverText)
                        val text = decrCipher.doFinal(Base64.decode(serverText.toByteArray(), Base64.NO_WRAP)).decodeToString()
                        scope.launch(Dispatchers.Main) {
                            decr.text = text
                        }
                    } catch (e: Exception) {
                        out.close()
                        inputStream.close()
                        socket.close()
                    }
                }
            }
        }
    }
}

fun getServerPublicKey(): PublicKey {
    val publicBytes: ByteArray = Base64.decode(Store.serverKey.toByteArray(), Base64.NO_WRAP)
    val keySpec = X509EncodedKeySpec(publicBytes)
    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePublic(keySpec)
}

object Store {
    lateinit var myPrivateKey: PrivateKey
    lateinit var myPublicKey: PublicKey
    lateinit var serverKey: String
    lateinit var serverExponent: String
    lateinit var myPublicEncoded: ByteArray
}
