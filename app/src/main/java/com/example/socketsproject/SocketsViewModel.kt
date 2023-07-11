package com.example.socketsproject

import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val SYM_START = "SIMMETYC_KEY = "
private const val SYM_END = "/SIMMETYC_KEY "

class SocketsViewModel : ViewModel() {

    val livedata = MutableLiveData<String>()
    val viewModelScope = CoroutineScope(Dispatchers.IO)

    private val assymetricHelper = AsymmetricGeneratorHelper()
        .apply { createKeys() }

    private val symmetricHelper = SymmetricHelper()

    private lateinit var inputStream: BufferedReader
    private lateinit var out: BufferedWriter

    private lateinit var socket: Socket

    fun createSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!this@SocketsViewModel::socket.isInitialized) {
                try {
                    socket = Socket("192.168.100.9", 33876)
                    writeAssymetricKeysToServer()
                    inputStream = BufferedReader(InputStreamReader(socket.getInputStream()))
                    getServerAssymetricKey()
                    assymetricHelper.initCiphers()
                } catch (e: Exception) {
                    Log.e("TAG23", "not error ${e.message}")
                }
            }
        }
    }

    fun onSendBtnClicked(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (socket.isConnected) {
                try {
                    symmetricHelper.createSymmetricKey()
                    writeSymmerticKey(text)
                    val takedText = getServerMessage()
                    viewModelScope.launch(Dispatchers.Main) {
                        livedata.postValue(takedText)
                    }
                } catch (e: Exception) {
                    out.close()
                    inputStream.close()
                    socket.close()
                }
            }
        }
    }

    private fun writeAssymetricKeysToServer() {
        out = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
        out.write(
            "${
                Base64.encode(Store.myPublicEncoded, Base64.DEFAULT).decodeToString()
                    .replace("\n", "")
            }\r"
        )
        out.flush()
    }

    private fun getServerAssymetricKey() {
        val text = inputStream.readLine().replace("\r", "")
        Store.serverKey = text
    }

    private fun writeSymmerticKey(sendText: String) {
        val sendEncryptText = Base64Helper.encode(symmetricHelper.enctypt(sendText))
        val encSimm = Base64Helper.encode(assymetricHelper.encode())
        out.write("$SYM_START${encSimm}$SYM_END${sendEncryptText}\r")
        out.flush()
    }

    private fun getServerMessage(): String {
        val simmetrycKeyString = inputStream.readLine().replace("\r", "")
        val simmetrycKey = simmetrycKeyString.substringSymmetrinc(SYM_START, SYM_END)
        symmetricHelper.initChiper(assymetricHelper.decode(simmetrycKey))
        val serverText = simmetrycKeyString.substringFullLength(SYM_END)
        return symmetricHelper.decode(serverText)
    }
}