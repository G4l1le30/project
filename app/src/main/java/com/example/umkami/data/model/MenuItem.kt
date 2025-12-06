package com.example.umkami.data.model

data class MenuItem(
    val name: String = "",
    val price: Int = 0,
    var umkmId: String = "" // Changed from val to var
)