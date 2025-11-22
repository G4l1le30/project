// viewmodel/HomeViewModel.kt

package com.example.umkami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.Umkm
import com.example.umkami.data.repository.UmkmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = UmkmRepository()

    // StateFlow untuk menampung list UMKM. Diinisialisasi dengan list kosong.
    private val _umkmList = MutableStateFlow<List<Umkm>>(emptyList())
    val umkmList: StateFlow<List<Umkm>> = _umkmList

    init {
        // Memuat data segera setelah ViewModel dibuat
        loadUmkmList()
    }

    // Fungsi untuk memuat data dari repository secara asinkron
    private fun loadUmkmList() {
        viewModelScope.launch {
            val list = repository.getUmkmFromFirebase()
            _umkmList.value = list
        }
    }
}