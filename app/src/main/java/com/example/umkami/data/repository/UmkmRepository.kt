package com.example.umkami.data.repository

import com.example.umkami.data.model.Umkm
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UmkmRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getUmkmFromFirebase(): List<Umkm> {
        return try {
            val snapshot = db.collection("umkm").get().await()
            snapshot.toObjects(Umkm::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
