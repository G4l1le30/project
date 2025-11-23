package com.example.umkami.data.model

data class CartItem(
    val item: MenuItem, // Bisa juga dibuat generik untuk ServiceItem jika perlu
    val quantity: Int
)
