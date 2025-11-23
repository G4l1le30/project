package com.example.umkami.data.model

data class Review(
    // Default values are added to handle potential nulls from Firebase
    val author: String = "Anonymous",
    val comment: String = "",
    val rating: Float = 0.0f
)
