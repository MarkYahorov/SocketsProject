package com.example.socketsproject

fun String.substringFullLength(text: String): String {
    val index = indexOf(text)
   return substring(
       index + text.length, endIndex = length
    )
}

fun String.substringSymmetrinc(text: String, secondKey: String): String {
    return substring(
        indexOf(text) + text.length, endIndex = indexOf(secondKey)
    )
}