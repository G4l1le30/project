package com.example.umkami.data.model

// Data class untuk menyimpan informasi pengguna
data class User(
    var uid: String = "",
    var email: String = "",
    var displayName: String = "", // Nama tampilan pengguna
    var address: String = "" // New field for user's primary address
)