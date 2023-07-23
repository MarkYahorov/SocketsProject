package com.example.socketsproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider


class MainActivity : AppCompatActivity() {

    private lateinit var sendBtn: Button
    private lateinit var input: EditText
    private lateinit var textView: TextView
    private lateinit var decr: TextView
    private val vm by lazy {
        ViewModelProvider(this)[SocketsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vm.createSocket()


        sendBtn = findViewById(R.id.send)
        input = findViewById(R.id.editText)
        textView = findViewById(R.id.encrypt_text)
        decr = findViewById(R.id.decrypt_text)

        input.doOnTextChanged { text, _, _, _ ->
            sendBtn.isEnabled = text.toString().isNotBlank()
        }

        sendBtn.setOnClickListener {
            vm.onSendBtnClicked(input.text.toString())
        }

        vm.livedata.observe(this) {
            decr.text = it
        }

        vm.errorLiveData.observe(this) {
            if (it.isNotBlank()) {
                showError(it)
            }
        }
    }

    override fun onDestroy() {
        vm.closeAllStreams()

        super.onDestroy()
    }

    private fun showError(error: String) {
        AlertDialog.Builder(this)
            .setTitle(error)
            .setPositiveButton("Лады") { dialog, id ->
                dialog.cancel()
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}
