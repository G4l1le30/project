package com.example.umkami.data.model

data class CartItem(
    val item: MenuItem,
    val quantity: Int,
    val umkmId: String,
    val umkmName: String
)
