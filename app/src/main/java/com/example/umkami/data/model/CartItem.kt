package com.example.umkami.data.model

data class CartItem(
    val item: MenuItem = MenuItem(),
    val quantity: Int = 0,
    val umkmId: String = "",
    val umkmName: String = ""
)
