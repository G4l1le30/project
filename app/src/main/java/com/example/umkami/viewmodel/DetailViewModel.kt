package com.example.umkami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.MenuItem
import com.example.umkami.data.model.ServiceItem
import com.example.umkami.data.model.Umkm
import com.example.umkami.data.repository.UmkmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 1. STATE GABUNGAN (DIGUNAKAN OLEH DETAIL SCREEN)
data class DetailUiState(
    val umkm: Umkm? = null,
    val menuItems: List<MenuItem> = emptyList(), // Untuk Makanan/Minuman
    val serviceItems: List<ServiceItem> = emptyList(), // Untuk Jasa/Kerajinan
    val isLoading: Boolean = true
)

class DetailViewModel(private val umkmId: String) : ViewModel() {
    private val repository = UmkmRepository()

    // 2. EXPOSED STATE: Hanya ekspos _uiState (model gabungan)
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState

    // ❌ HAPUS: Hapus _umkmDetail dan umkmDetail (state lama yang duplikat)
    // private val _umkmDetail = MutableStateFlow<Umkm?>(null)
    // val umkmDetail: StateFlow<Umkm?> = _umkmDetail


    init {
        // PERBAIKAN: Ganti panggilan ke fungsi yang memuat semua data
        loadDetailData(umkmId)
    }

    // PERBAIKAN: Ubah nama fungsi agar lebih jelas
    private fun loadDetailData(id: String) {
        // Mulai loading, atur isLoading = true
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // 1. Ambil data inti UMKM
                val umkm = repository.getUmkmById(id)

                if (umkm == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }

                var menuList = emptyList<MenuItem>()
                var serviceList = emptyList<ServiceItem>()

                // 2. Tentukan data spesialisasi berdasarkan kategori UMKM
                when (umkm.category) {
                    "Makanan", "Minuman" -> {
                        menuList = repository.getUmkmMenu(id)
                    }
                    "Jasa", "Kerajinan", "Fashion" -> {
                        serviceList = repository.getUmkmServices(id)
                    }
                }

                // 3. Update state akhir
                _uiState.value = _uiState.value.copy(
                    umkm = umkm,
                    menuItems = menuList,
                    serviceItems = serviceList,
                    isLoading = false // Loading selesai
                )

            } catch (e: Exception) {
                println("ERROR fetching UMKM detail for $id: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // Factory tetap sama
    companion object {
        fun Factory(umkmId: String) = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DetailViewModel(umkmId) as T
            }
        }
    }
}