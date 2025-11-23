package com.example.umkami.data.model

// Data class ini harus sesuai dengan struktur field di Firestore collection "umkm"
data class Umkm(
    // Penting: Firebase membutuhkan konstruktor tanpa argumen
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var category: String = "", // misal: makanan, jasa, minuman
    var address: String = "",
    var imageUrl: String = "", // Untuk dimuat oleh Coil di HomeScreen.kt
    var lat: Double = 0.0, // Atau String/Float, tergantung tipe data di JSON Anda
    var lng: Double = 0.0,
    var contact: String = "" // New field for contact number
)