package com.example.socketsproject

import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

object Store {
    lateinit var myPrivateKey: PrivateKey
    lateinit var myPublicKey: PublicKey
    lateinit var serverKey: String
    lateinit var simmetrycMyKey: SecretKey
    lateinit var myPublicEncoded: ByteArray
    lateinit var simmetrycKeyEncoded: ByteArray
    lateinit var simmetrycServerKey: SecretKey
}