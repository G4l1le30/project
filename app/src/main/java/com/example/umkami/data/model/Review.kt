package com.example.umkami.data.model

data class Review(
    // Menggunakan key "comment" untuk menampung teks ulasan
    // Firebase akan mengembalikan Map<String, String> di node umkm0,
    // jadi kita bisa memetakan langsung nilai String-nya.
    // Namun, untuk fleksibilitas, kita akan simpan sebagai String.
    val comment: String = "",
    val reviewerName: String = "Pengguna UMKami", // Default name for MVP
    val rating: Double = 0.0 // Default rating
)