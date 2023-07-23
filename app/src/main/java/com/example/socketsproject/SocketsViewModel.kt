package com.example.socketsproject

import android.util.Base64
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


class SocketsViewModel : ViewModel() {

    val livedata = MutableLiveData<String>()
    val errorLiveData = MutableLiveData<String>()
    val viewModelScope = CoroutineScope(Dispatchers.IO)

    private val assymetricHelper = AsymmetricGeneratorHelper()
        .apply { createKeys() }

    private lateinit var inputStream: BufferedReader
    private lateinit var out: BufferedWriter

    private val socket by lazy {
        Socket("192.168.100.9", 33876)
    }

    fun createSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                writeAssymetricKeysToServer()
                inputStream = BufferedReader(InputStreamReader(socket.getInputStream()))
                val assymetric = getServerAssymetricKey()
                assymetricHelper.apply {
                    initCiphers(assymetric)
                }

            } catch (e: Exception) {
                catchError(e)
            }
        }
    }

    fun onSendBtnClicked(sendText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (socket.isConnected) {
                try {
                    assymetricHelper.sendText(out, sendText)
                    val takedText = assymetricHelper.readText(inputStream, socket.getInputStream())
                    viewModelScope.launch(Dispatchers.Main) {
                        livedata.postValue(takedText)
                    }
                } catch (e: Exception) {
                    catchError(e)
                }
            }
        }
    }

    private fun writeAssymetricKeysToServer() {
        out = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
        out.write(
            "${
                Base64.encode(assymetricHelper.getPublickKey(), Base64.DEFAULT).decodeToString()
                    .replace("\n", "")
            }\r"
        )
        out.flush()
    }

    private fun getServerAssymetricKey(): String {
        return inputStream.readLine().replace("\r", "")
    }

    private fun catchError(e: Exception) {
        viewModelScope.launch(Dispatchers.Main) {
            errorLiveData.postValue(e.message)
        }
        closeAllStreams()
    }

    fun closeAllStreams() {
        out.close()
        inputStream.close()
        socket.close()
    }
}