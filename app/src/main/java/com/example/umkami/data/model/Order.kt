package com.example.umkami.data.model

data class Order(
    val userId: String = "", // New field to link order to user
    val umkmId: String,
    val items: List<CartItem>,
    val totalPrice: Double,
    val orderTimestamp: Long = System.currentTimeMillis(),
    val customerName: String = "Anonymous Customer"
)
