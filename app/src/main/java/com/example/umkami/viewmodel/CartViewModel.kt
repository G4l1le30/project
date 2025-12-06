package com.example.umkami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.CartItem
import com.example.umkami.data.model.MenuItem
import com.example.umkami.data.model.Order
import com.example.umkami.data.model.User
import com.example.umkami.data.repository.UmkmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    private val repository = UmkmRepository()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // State for UI
    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    private val _checkoutError = MutableStateFlow<String?>(null)
    val checkoutError: StateFlow<String?> = _checkoutError.asStateFlow()

    private val _checkoutSuccess = MutableStateFlow(false)
    val checkoutSuccess: StateFlow<Boolean> = _checkoutSuccess.asStateFlow()

    val totalPrice: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.item.price.toDouble() * it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val groupedCartItems: StateFlow<Map<String, List<CartItem>>> = _cartItems.map { items ->
        items.groupBy { it.umkmName }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    fun addItem(menuItem: MenuItem, umkmName: String) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.item.name == menuItem.name && it.umkmId == menuItem.umkmId }

        if (existingItem != null) {
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
            val itemIndex = currentItems.indexOf(existingItem)
            currentItems[itemIndex] = updatedItem
        } else {
            currentItems.add(CartItem(item = menuItem, quantity = 1, umkmId = menuItem.umkmId, umkmName = umkmName))
        }
        _cartItems.value = currentItems
    }

    fun removeItem(menuItem: MenuItem, umkmName: String) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.item.name == menuItem.name && it.umkmId == menuItem.umkmId }

        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                val updatedItem = existingItem.copy(quantity = existingItem.quantity - 1)
                val itemIndex = currentItems.indexOf(existingItem)
                currentItems[itemIndex] = updatedItem
            } else {
                currentItems.remove(existingItem)
            }
            _cartItems.value = currentItems
        }
    }

    fun placeOrder(user: User) {
        if (_cartItems.value.isEmpty() || user.uid.isBlank()) {
            _checkoutError.value = "Keranjang kosong atau pengguna tidak valid."
            return
        }

        viewModelScope.launch {
            _isPlacingOrder.value = true
            _checkoutError.value = null
            _checkoutSuccess.value = false

            try {
                // Proses setiap UMKM secara terpisah
                val groupedOrders = _cartItems.value.groupBy { it.umkmId }
                var allOrdersSuccessful = true

                for ((umkmId, itemsForUmkm) in groupedOrders) {
                    val umkmTotalPrice = itemsForUmkm.sumOf { it.item.price.toDouble() * it.quantity }
                    
                    if (user.balance < umkmTotalPrice) {
                        _checkoutError.value = "Saldo tidak cukup untuk menyelesaikan pesanan dari salah satu UMKM."
                        allOrdersSuccessful = false
                        break 
                    }

                    val newOrder = Order(
                        userId = user.uid,
                        umkmId = umkmId,
                        items = itemsForUmkm,
                        totalPrice = umkmTotalPrice,
                        orderTimestamp = System.currentTimeMillis(),
                        customerName = user.displayName ?: "Customer"
                    )
                    
                    val result = repository.placeOrderWithBalanceCheck(newOrder)

                    if (result.isFailure) {
                        _checkoutError.value = result.exceptionOrNull()?.message ?: "Terjadi kesalahan saat memesan."
                        allOrdersSuccessful = false
                        break // Hentikan jika satu pesanan gagal
                    }
                }

                if (allOrdersSuccessful) {
                    _cartItems.value = emptyList() // Bersihkan keranjang jika semua pesanan berhasil
                    _checkoutSuccess.value = true
                }

            } catch (e: Exception) {
                _checkoutError.value = e.message ?: "Terjadi error tidak terduga."
            } finally {
                _isPlacingOrder.value = false
            }
        }
    }

    fun clearError() {
        _checkoutError.value = null
    }


    fun clearCheckoutSuccess() {
        _checkoutSuccess.value = false
    }
}