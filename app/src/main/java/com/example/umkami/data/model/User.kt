package com.example.umkami.data.model

// Data class untuk menyimpan informasi pengguna
data class User(
    var uid: String = "",
    var email: String? = null,
    var displayName: String? = null, // Nama tampilan pengguna
    var address: String? = null // New field for user's primary address
)