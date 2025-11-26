package com.example.umkami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.Umkm
import com.example.umkami.data.repository.UmkmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WishlistViewModel : ViewModel() {
    private val repository = UmkmRepository()

    private val _wishlist = MutableStateFlow<List<Umkm>>(emptyList())
    val wishlist: StateFlow<List<Umkm>> = _wishlist

    fun loadWishlist(userId: String) {
        viewModelScope.launch {
            val wishlistIds = repository.getWishlist(userId)
            val wishlistItems = mutableListOf<Umkm>()
            for (id in wishlistIds) {
                val umkm = repository.getUmkmById(id)
                if (umkm != null) {
                    wishlistItems.add(umkm)
                }
            }
            _wishlist.value = wishlistItems
        }
    }
}
