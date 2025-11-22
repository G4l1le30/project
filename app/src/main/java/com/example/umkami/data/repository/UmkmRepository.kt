package com.example.umkami.data.repository

import com.example.umkami.data.model.Umkm
import com.google.firebase.database.FirebaseDatabase // Ganti: Import Realtime Database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class UmkmRepository {

    // Inisialisasi referensi ke node "umkm" di Realtime Database
    private val dbRef = FirebaseDatabase.getInstance().getReference("umkm")

    // Fungsi suspend untuk mengambil data dari Realtime Database
    suspend fun getUmkmFromFirebase(): List<Umkm> = suspendCancellableCoroutine { continuation ->

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val umkmList = mutableListOf<Umkm>()

                // Iterasi melalui setiap anak (misal: umkm0, umkm1)
                for (umkmSnapshot in snapshot.children) {
                    // Coba konversi data snapshot ke objek Umkm
                    val umkm = umkmSnapshot.getValue(Umkm::class.java)
                    if (umkm != null) {
                        umkm.id = umkmSnapshot.key ?: ""
                        umkmList.add(umkm)
                    }
                }

                // Melanjutkan coroutine dengan list hasil
                if (continuation.isActive) {
                    continuation.resume(umkmList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Melanjutkan dengan list kosong jika terjadi error
                if (continuation.isActive) {
                    continuation.resume(emptyList())
                }
            }
        })
    }
}