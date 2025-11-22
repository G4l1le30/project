package com.example.umkami.data.repository

import com.example.umkami.data.model.Umkm
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.example.umkami.data.model.MenuItem
import com.example.umkami.data.model.ServiceItem

class UmkmRepository {

    // PERBAIKAN: Mengarah ke root database
    private val dbRoot = FirebaseDatabase.getInstance().getReference()

    // Referensi untuk data umum
    private val dbUmkm = dbRoot.child("umkm")
    // Referensi untuk data spesialisasi
    private val dbMenu = dbRoot.child("umkm_menu")
    private val dbService = dbRoot.child("umkm_services")

    // ===============================================
    // FUNGSI INTI
    // ===============================================

    // Fungsi suspend untuk mengambil SEMUA data dari Realtime Database
    suspend fun getUmkmFromFirebase(): List<Umkm> = suspendCancellableCoroutine { continuation ->
        dbUmkm.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val umkmList = mutableListOf<Umkm>()
                for (umkmSnapshot in snapshot.children) {
                    try {
                        val umkm = umkmSnapshot.getValue(Umkm::class.java)
                        if (umkm != null) {
                            umkm.id = umkmSnapshot.key ?: ""
                            umkmList.add(umkm)
                        } else {
                            // Tambahkan logging untuk data null
                            println("DEBUG: Gagal mem-parsing UMKM. Objek null ditemukan di key: ${umkmSnapshot.key}")
                        }
                    } catch (e: Exception) {
                        // Tambahkan logging jika terjadi error deserialisasi
                        println("ERROR MAPPING: Gagal memetakan data UMKM untuk key: ${umkmSnapshot.key}. Error: ${e.message}")
                    }
                }
                if (continuation.isActive) continuation.resume(umkmList)
            }
            override fun onCancelled(error: DatabaseError) {
                if (continuation.isActive) continuation.resume(emptyList())
            }
        })
    }

    // Fungsi suspend untuk mengambil data UMKM berdasarkan ID
    suspend fun getUmkmById(umkmId: String): Umkm? = suspendCancellableCoroutine { continuation ->
        // Menggunakan dbUmkm
        dbUmkm.child(umkmId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val umkm = snapshot.getValue(Umkm::class.java)
                if (umkm != null) umkm.id = snapshot.key ?: umkmId
                if (continuation.isActive) continuation.resume(umkm)
            }
            override fun onCancelled(error: DatabaseError) {
                if (continuation.isActive) continuation.resume(null)
            }
        })
    }

    // ===============================================
    // FUNGSI SPESIALISASI BARU (Menu & Jasa)
    // ===============================================

    // FUNGSI BARU: Mendapatkan Menu Makanan/Minuman
    suspend fun getUmkmMenu(umkmId: String): List<MenuItem> = suspendCancellableCoroutine { continuation ->
        // Akses node umkm_menu/umkmId
        dbMenu.child(umkmId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Mapping children ke List<MenuItem>
                val menuList = snapshot.children.mapNotNull {
                    it.getValue(MenuItem::class.java)
                }
                if (continuation.isActive) continuation.resume(menuList)
            }
            override fun onCancelled(error: DatabaseError) {
                if (continuation.isActive) continuation.resume(emptyList())
            }
        })
    }

    // FUNGSI BARU: Mendapatkan Paket Jasa
    suspend fun getUmkmServices(umkmId: String): List<ServiceItem> = suspendCancellableCoroutine { continuation ->
        // Akses node umkm_services/umkmId
        dbService.child(umkmId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Mapping children ke List<ServiceItem>
                val serviceList = snapshot.children.mapNotNull {
                    it.getValue(ServiceItem::class.java)
                }
                if (continuation.isActive) continuation.resume(serviceList)
            }
            override fun onCancelled(error: DatabaseError) {
                if (continuation.isActive) continuation.resume(emptyList())
            }
        })
    }
}