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
import kotlinx.coroutines.flow.SharingStarted // Added this import
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    private val repository = UmkmRepository() // To place the order later

    // Backing property for cart items
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    // Publicly exposed immutable StateFlow for the UI to observe
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Total price is reactively calculated from cartItems
    val totalPrice: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.item.price.toDouble() * it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun addItem(menuItem: MenuItem) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.item.name == menuItem.name }

        if (existingItem != null) {
            // Item exists, increase quantity
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
            val itemIndex = currentItems.indexOf(existingItem)
            currentItems[itemIndex] = updatedItem
        } else {
            // Item does not exist, add as new
            currentItems.add(CartItem(item = menuItem, quantity = 1))
        }
        _cartItems.value = currentItems
    }

    fun removeItem(menuItem: MenuItem) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.item.name == menuItem.name }

        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                // Item quantity > 1, decrease quantity
                val updatedItem = existingItem.copy(quantity = existingItem.quantity - 1)
                val itemIndex = currentItems.indexOf(existingItem)
                currentItems[itemIndex] = updatedItem
            } else {
                // Item quantity is 1, remove from cart
                currentItems.remove(existingItem)
            }
            _cartItems.value = currentItems
        }
    }

    fun placeOrder(umkmId: String, callback: (Boolean) -> Unit) {
        if (_cartItems.value.isEmpty()) {
            callback(false)
            return
        }

        val newOrder = Order(
            umkmId = umkmId,
            items = _cartItems.value,
            totalPrice = totalPrice.value
        )

        viewModelScope.launch {
            val success = repository.placeOrder(newOrder)
            if (success) {
                _cartItems.value = emptyList() // Clear cart on successful order
            }
            callback(success)
        }
    }
}
