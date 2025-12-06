package com.example.umkami.data.model

// Data class untuk menyimpan informasi pengguna
data class User(
    var uid: String = "",
    var email: String? = null,
    var displayName: String? = null,
    var address: String? = null,
    var role: String = "customer", // "customer" or "owner"
    var umkmId: String? = null, // Associated UMKM ID for owners
    var balance: Double = 0.0 // User's balance for payments
)