package com.example.umkami.data.repository

import android.util.Log
import com.example.umkami.data.model.Umkm
import com.example.umkami.data.model.MenuItem
import com.example.umkami.data.model.ServiceItem
import com.google.firebase.database.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class UmkmRepository {

    // Root reference
    private val dbRoot = FirebaseDatabase.getInstance().reference

    // Child references
    private val dbUmkm = dbRoot.child("umkm")
    private val dbMenu = dbRoot.child("umkm_menu")
    private val dbService = dbRoot.child("umkm_services")
    private val dbReviews = dbRoot.child("reviews")

    // ============================================================
    // 1. Ambil semua UMKM
    // ============================================================
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
                            Log.e("UmkmRepository", "Null object at key: ${umkmSnapshot.key}")
                        }
                    } catch (e: Exception) {
                        Log.e("UmkmRepository", "Mapping error at key ${umkmSnapshot.key}: ${e.message}")
                    }
                }

                if (continuation.isActive) continuation.resume(umkmList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UmkmRepository", "Error: ${error.message}")
                if (continuation.isActive) continuation.resume(emptyList())
            }
        })
    }

    // ============================================================
    // 2. Ambil UMKM berdasarkan ID
    // ============================================================
    suspend fun getUmkmById(umkmId: String): Umkm? = suspendCancellableCoroutine { continuation ->
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

    // ============================================================
    // 3. Ambil Menu UMKM
    // ============================================================
    suspend fun getUmkmMenu(umkmId: String): List<MenuItem> = suspendCancellableCoroutine { continuation ->
        dbMenu.child(umkmId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val menuList = snapshot.children.mapNotNull { it.getValue(MenuItem::class.java) }
                if (continuation.isActive) continuation.resume(menuList)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(emptyList())
            }
        })
    }

    // ============================================================
    // 4. Ambil Layanan UMKM
    // ============================================================
    suspend fun getUmkmServices(umkmId: String): List<ServiceItem> = suspendCancellableCoroutine { continuation ->
        dbService.child(umkmId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val serviceList = snapshot.children.mapNotNull { it.getValue(ServiceItem::class.java) }
                if (continuation.isActive) continuation.resume(serviceList)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(emptyList())
            }
        })
    }

    // ============================================================
    // 5. Ambil Reviews UMKM (Coroutine)
    // ============================================================
    suspend fun getReviewsByUmkmId(umkmId: String): List<String> = suspendCancellableCoroutine { continuation ->
        dbReviews.child(umkmId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val reviewList = snapshot.children.mapNotNull {
                    it.getValue(String::class.java)
                }

                if (continuation.isActive) continuation.resume(reviewList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UmkmRepository", "Error getting reviews: ${error.message}")
                if (continuation.isActive) continuation.resume(emptyList())
            }
        })
    }

    // ============================================================
    // ALTERNATIF: Callback (Kalau Compose kamu butuh callback)
    // ============================================================
    fun getReviewsByUmkmIdCallback(umkmId: String, onResult: (List<String>) -> Unit) {
        dbReviews.child(umkmId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reviewList = snapshot.children.mapNotNull {
                    it.getValue(String::class.java)
                }
                onResult(reviewList)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }
}
