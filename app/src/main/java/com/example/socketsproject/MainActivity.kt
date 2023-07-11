package com.example.socketsproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider


class MainActivity : AppCompatActivity() {

    private lateinit var sendBtn: Button
    private lateinit var input: EditText
    private lateinit var textView: TextView
    private lateinit var decr: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val vm = ViewModelProvider(this)[SocketsViewModel::class.java]

        vm.createSocket()


        sendBtn = findViewById(R.id.send)
        input = findViewById(R.id.editText)
        textView = findViewById(R.id.encrypt_text)
        decr = findViewById(R.id.decrypt_text)

        sendBtn.setOnClickListener {
            vm.onSendBtnClicked(input.text.toString())
        }

        vm.livedata.observe(this){
            decr.text = it
        }
    }
}
