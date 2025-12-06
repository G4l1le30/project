package com.example.umkami.data.repository

import android.util.Log
import com.example.umkami.data.model.Umkm
import com.example.umkami.data.model.MenuItem
import com.example.umkami.data.model.Order
import com.example.umkami.data.model.Review
import com.example.umkami.data.model.ServiceItem
import com.google.firebase.database.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UmkmRepository {

    // Root reference
    private val dbRoot = FirebaseDatabase.getInstance().reference

    // Child references
    private val dbUmkm = dbRoot.child("umkm")
    private val dbUsers = dbRoot.child("users")
    private val dbMenu = dbRoot.child("umkm_menu")
    private val dbService = dbRoot.child("umkm_services")
    private val dbReviews = dbRoot.child("reviews")
    private val dbOrders = dbRoot.child("orders")
    private val dbCarts = dbRoot.child("carts")
    private val dbWishlist = dbRoot.child("wishlist")

    // ============================================================
    // 1. Ambil semua UMKM
    // ============================================================
    suspend fun getUmkmFromFirebase(): List<Umkm> {
        return try {
            val snapshot = dbUmkm.get().await()
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
            umkmList
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error getting all UMKM from Firebase: ${e.message}")
            emptyList()
        }
    }

    // ============================================================
    // 2. Ambil UMKM berdasarkan ID
    // ============================================================
    suspend fun getUmkmById(umkmId: String): Umkm? {
        return try {
            val snapshot = dbUmkm.child(umkmId).get().await()
            val umkm = snapshot.getValue(Umkm::class.java)
            umkm?.apply { id = snapshot.key ?: umkmId }
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error getting UMKM by ID: $e")
            null
        }
    }

    // ============================================================
    // 3. Ambil Menu UMKM
    // ============================================================
    suspend fun getUmkmMenu(umkmId: String): List<MenuItem> {
        return try {
            val snapshot = dbMenu.child(umkmId).get().await()
            snapshot.children.mapNotNull { dataSnapshot ->
                dataSnapshot.getValue(MenuItem::class.java)?.apply {
                    this.umkmId = umkmId // Inject the umkmId into each menu item
                }
            }
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error getting UMKM menu: $e")
            emptyList()
        }
    }

    // ============================================================
    // 4. Ambil Layanan UMKM
    // ============================================================
    suspend fun getUmkmServices(umkmId: String): List<ServiceItem> {
        return try {
            val snapshot = dbService.child(umkmId).get().await()
            snapshot.children.mapNotNull { it.getValue(ServiceItem::class.java) }
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error getting UMKM services: $e")
            emptyList()
        }
    }

    // ============================================================
    // 5. Ambil Reviews UMKM (Coroutine)
    // ============================================================
    suspend fun getReviewsByUmkmId(umkmId: String): List<Review> {
        return try {
            val snapshot = dbReviews.child(umkmId).get().await()
            val reviewList = mutableListOf<Review>()
            snapshot.children.forEach { child ->
                val review = try { child.getValue(Review::class.java) } catch (e: Exception) { null }
                if (review != null) {
                    reviewList.add(review)
                } else {
                    val oldReview = child.getValue(String::class.java)
                    if (oldReview != null) {
                        reviewList.add(Review(author = "Anonymous", comment = oldReview, rating = 3.0f))
                    }
                }
            }
            reviewList
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error getting UMKM reviews: $e")
            emptyList()
        }
    }

    // ============================================================
    // 6. Tambah Review Baru
    // ============================================================
    suspend fun addReview(umkmId: String, review: Review): Boolean {
        return try {
            dbReviews.child(umkmId).push().setValue(review).await()
            true
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error adding review: $e")
            false
        }
    }

    // ============================================================
    // 7. Tambah Order Baru (dengan Pengecekan Saldo)
    // ============================================================
    suspend fun placeOrderWithBalanceCheck(order: Order): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val userBalanceRef = dbUsers.child(order.userId).child("balance")

        userBalanceRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentBalance = mutableData.getValue(Double::class.java) ?: 0.0
                if (currentBalance < order.totalPrice) {
                    return Transaction.abort()
                }
                val newBalance = currentBalance - order.totalPrice
                mutableData.value = newBalance
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                databaseError: DatabaseError?,
                committed: Boolean,
                dataSnapshot: DataSnapshot?
            ) {
                if (databaseError != null) {
                    if (continuation.isActive) continuation.resumeWithException(databaseError.toException())
                    return
                }

                if (committed) {
                    val newOrderRef = dbOrders.push()
                    newOrderRef.setValue(order).addOnCompleteListener { orderTask ->
                        if (orderTask.isSuccessful) {
                            dbCarts.child(order.userId).removeValue().addOnCompleteListener { cartTask ->
                                if (cartTask.isSuccessful) {
                                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                                } else {
                                    if (continuation.isActive) continuation.resume(Result.failure(cartTask.exception ?: Exception("Gagal menghapus keranjang.")))
                                }
                            }
                        } else {
                            if (continuation.isActive) continuation.resume(Result.failure(orderTask.exception ?: Exception("Gagal membuat pesanan.")))
                        }
                    }
                } else {
                    if (continuation.isActive) continuation.resume(Result.failure(Exception("Saldo tidak cukup.")))
                }
            }
        })
    }
    
    // ============================================================
    // 8. Get Orders by User ID
    // ============================================================
    suspend fun getOrdersByUserId(userId: String): List<Order> {
        return try {
            val snapshot = dbOrders.orderByChild("userId").equalTo(userId).get().await()
            snapshot.children.mapNotNull { it.getValue(Order::class.java) }.sortedByDescending { it.orderTimestamp }
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error getting orders by user ID: $e")
            emptyList()
        }
    }

    suspend fun saveUmkm(umkm: Umkm, userId: String): String {
        return try {
            val umkmToSave = umkm.copy()
            var umkmId = umkmToSave.id

            if (umkmId.isBlank()) {
                umkmId = dbUmkm.push().key ?: ""
                umkmToSave.id = umkmId
            }

            dbUmkm.child(umkmId).setValue(umkmToSave).await()
            dbUsers.child(userId).child("umkmId").setValue(umkmId).await()
            umkmId
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error saving UMKM: $e")
            ""
        }
    }

    suspend fun topUpBalance(userId: String, amount: Double): Result<Unit> = suspendCancellableCoroutine { continuation ->
        if (userId.isBlank() || amount <= 0) {
            if (continuation.isActive) continuation.resume(Result.failure(IllegalArgumentException("User ID tidak valid atau jumlah top-up tidak positif.")))
            return@suspendCancellableCoroutine
        }

        val userBalanceRef = dbUsers.child(userId).child("balance")

        userBalanceRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentBalance = mutableData.getValue(Double::class.java) ?: 0.0
                val newBalance = currentBalance + amount
                mutableData.value = newBalance
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                databaseError: DatabaseError?,
                committed: Boolean,
                dataSnapshot: DataSnapshot?
            ) {
                if (databaseError != null) {
                    if (continuation.isActive) continuation.resume(Result.failure(databaseError.toException()))
                } else if (committed) {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                } else {
                    if (continuation.isActive) continuation.resume(Result.failure(Exception("Transaksi top-up gagal, tidak di-commit.")))
                }
            }
        })
    }

    // ============================================================
    // 9. Wishlist
    // ============================================================
    suspend fun addToWishlist(userId: String, umkmId: String): Boolean {
        return try {
            dbWishlist.child(userId).child(umkmId).setValue(true).await()
            true
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error adding to wishlist: $e")
            false
        }
    }

    suspend fun removeFromWishlist(userId: String, umkmId: String): Boolean {
        return try {
            dbWishlist.child(userId).child(umkmId).removeValue().await()
            true
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error removing from wishlist: $e")
            false
        }
    }

    suspend fun getWishlist(userId: String): List<String> {
        return try {
            val snapshot = dbWishlist.child(userId).get().await()
            snapshot.children.mapNotNull { it.key }
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error getting wishlist: $e")
            emptyList()
        }
    }

    suspend fun isWishlisted(userId: String, umkmId: String): Boolean {
        return try {
            val snapshot = dbWishlist.child(userId).child(umkmId).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error checking if UMKM is wishlisted: $e")
            false
        }
    }

    suspend fun saveMenu(umkmId: String, menu: List<MenuItem>): Boolean {
        return try {
            dbMenu.child(umkmId).setValue(menu).await()
            true
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error saving menu: $e")
            false
        }
    }

    suspend fun saveServices(umkmId: String, services: List<ServiceItem>): Boolean {
        return try {
            dbService.child(umkmId).setValue(services).await()
            true
        } catch (e: Exception) {
            Log.e("UmkmRepository", "Error saving services: $e")
            false
        }
    }
}
