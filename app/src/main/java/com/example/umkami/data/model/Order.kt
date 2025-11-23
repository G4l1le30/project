package com.example.umkami.data.model

data class Order(
    val umkmId: String,
    val items: List<CartItem>,
    val totalPrice: Double,
    val orderTimestamp: Long = System.currentTimeMillis(),
    // Bisa ditambahkan info lain seperti nama pemesan, nomor meja, dll.
    val customerName: String = "Anonymous Customer"
)
