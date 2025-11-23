package com.example.umkami.data.model

data class Order(
    val userId: String = "",
    val umkmId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val orderTimestamp: Long = System.currentTimeMillis(),
    val customerName: String = "Anonymous Customer"
)
