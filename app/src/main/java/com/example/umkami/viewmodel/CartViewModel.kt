package com.example.umkami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.CartItem
import com.example.umkami.data.model.MenuItem
import com.example.umkami.data.model.Order
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

    fun placeOrder(userId: String, callback: (Boolean) -> Unit) {
        if (_cartItems.value.isEmpty()) {
            callback(false)
            return
        }
        if (userId.isBlank()) {
            callback(false)
            return
        }

        viewModelScope.launch {
            val groupedOrders = _cartItems.value.groupBy { it.umkmId }
            var allOrdersSuccessful = true

            for ((umkmId, itemsForUmkm) in groupedOrders) {
                val umkmTotalPrice = itemsForUmkm.sumOf { it.item.price.toDouble() * it.quantity }
                
                val newOrder = Order(
                    userId = userId,
                    umkmId = umkmId,
                    items = itemsForUmkm,
                    totalPrice = umkmTotalPrice,
                    orderTimestamp = System.currentTimeMillis()
                )
                
                val success = repository.placeOrder(newOrder)
                if (!success) {
                    allOrdersSuccessful = false
                    break
                }
            }

            if (allOrdersSuccessful) {
                _cartItems.value = emptyList()
            }
            callback(allOrdersSuccessful)
        }
    }
}