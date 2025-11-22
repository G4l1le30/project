package com.example.umkami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.Umkm
import com.example.umkami.data.repository.UmkmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = UmkmRepository()

    // StateFlow daftar UMKM
    private val _umkmList = MutableStateFlow<List<Umkm>>(emptyList())
    val umkmList: StateFlow<List<Umkm>> = _umkmList.asStateFlow()

    init {
        loadUmkmList()
    }

    private fun loadUmkmList() {
        viewModelScope.launch {
            try {
                val list = repository.getUmkmFromFirebase()

                // Update state
                _umkmList.value = list

                // Debug jika kosong
                if (list.isEmpty()) {
                    println("DEBUG: Firebase returned empty UMKM list.")
                }

            } catch (e: Exception) {
                println("ERROR fetching UMKM list: ${e.message}")
                _umkmList.value = emptyList()
            }
        }
    }

    // Opsional: bisa ditambah filter kategori jika nanti pakai Search/Category
    fun filterByCategory(category: String) {
        val currentList = _umkmList.value
        _umkmList.value = currentList.filter { it.category == category }
    }
}
