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
                val menuList = snapshot.children.mapNotNull { dataSnapshot ->
                    dataSnapshot.getValue(MenuItem::class.java)?.apply {
                        this.umkmId = umkmId // Inject the umkmId into each menu item
                    }
                }
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
    suspend fun getReviewsByUmkmId(umkmId: String): List<Review> = suspendCancellableCoroutine { continuation ->
        dbReviews.child(umkmId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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
                if (continuation.isActive) continuation.resume(reviewList)
            }
            override fun onCancelled(error: DatabaseError) {
                if (continuation.isActive) continuation.resume(emptyList())
            }
        })
    }

    // ============================================================
    // 6. Tambah Review Baru
    // ============================================================
    suspend fun addReview(umkmId: String, review: Review): Boolean = suspendCancellableCoroutine { continuation ->
        dbReviews.child(umkmId).push().setValue(review)
            .addOnSuccessListener { if (continuation.isActive) continuation.resume(true) }
            .addOnFailureListener { if (continuation.isActive) continuation.resume(false) }
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
    suspend fun getOrdersByUserId(userId: String): List<Order> = suspendCancellableCoroutine { continuation ->
        dbOrders.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
                if (continuation.isActive) continuation.resume(orderList.sortedByDescending { it.orderTimestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                if (continuation.isActive) continuation.resume(emptyList())
            }
        })
    }

    suspend fun saveUmkm(umkm: Umkm, userId: String): String = suspendCancellableCoroutine { continuation ->
        val umkmToSave = umkm.copy()
        var umkmId = umkmToSave.id

        if (umkmId.isBlank()) {
            umkmId = dbUmkm.push().key ?: ""
            umkmToSave.id = umkmId
        }

        dbUmkm.child(umkmId).setValue(umkmToSave)
            .addOnSuccessListener {
                dbUsers.child(userId).child("umkmId").setValue(umkmId)
                    .addOnSuccessListener { if (continuation.isActive) continuation.resume(umkmId) }
                    .addOnFailureListener { e -> if (continuation.isActive) continuation.resume(umkmId) }
            }
            .addOnFailureListener { e -> if (continuation.isActive) continuation.resume("") }
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
    suspend fun addToWishlist(userId: String, umkmId: String): Boolean = suspendCancellableCoroutine { continuation ->
        dbWishlist.child(userId).child(umkmId).setValue(true)
            .addOnSuccessListener { if (continuation.isActive) continuation.resume(true) }
            .addOnFailureListener { if (continuation.isActive) continuation.resume(false) }
    }

    suspend fun removeFromWishlist(userId: String, umkmId: String): Boolean = suspendCancellableCoroutine { continuation ->
        dbWishlist.child(userId).child(umkmId).removeValue()
            .addOnSuccessListener { if (continuation.isActive) continuation.resume(true) }
            .addOnFailureListener { if (continuation.isActive) continuation.resume(false) }
    }

    suspend fun getWishlist(userId: String): List<String> = suspendCancellableCoroutine { continuation ->
        dbWishlist.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val wishlist = snapshot.children.mapNotNull { it.key }
                if (continuation.isActive) continuation.resume(wishlist)
            }
            override fun onCancelled(error: DatabaseError) { if (continuation.isActive) continuation.resume(emptyList()) }
        })
    }

    suspend fun isWishlisted(userId: String, umkmId: String): Boolean = suspendCancellableCoroutine { continuation ->
        dbWishlist.child(userId).child(umkmId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { if (continuation.isActive) continuation.resume(snapshot.exists()) }
            override fun onCancelled(error: DatabaseError) { if (continuation.isActive) continuation.resume(false) }
        })
    }

    suspend fun saveMenu(umkmId: String, menu: List<MenuItem>): Boolean = suspendCancellableCoroutine { continuation ->
        dbMenu.child(umkmId).setValue(menu)
            .addOnSuccessListener { if (continuation.isActive) continuation.resume(true) }
            .addOnFailureListener { if (continuation.isActive) continuation.resume(false) }
    }

    suspend fun saveServices(umkmId: String, services: List<ServiceItem>): Boolean = suspendCancellableCoroutine { continuation ->
        dbService.child(umkmId).setValue(services)
            .addOnSuccessListener { if (continuation.isActive) continuation.resume(true) }
            .addOnFailureListener { if (continuation.isActive) continuation.resume(false) }
    }
}
