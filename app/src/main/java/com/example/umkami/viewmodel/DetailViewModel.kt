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

// Tambahkan reviews ke UI State
data class DetailUiState(
    val umkm: Umkm? = null,
    val menuItems: List<MenuItem> = emptyList(),
    val serviceItems: List<ServiceItem> = emptyList(),
    val reviews: List<String> = emptyList(),   // BARU
    val isLoading: Boolean = true
)

class DetailViewModel(private val umkmId: String) : ViewModel() {

    private val repository = UmkmRepository()

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState

    init {
        loadDetailData(umkmId)
    }

    private fun loadDetailData(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // 1. Ambil data UMKM
                val umkm = repository.getUmkmById(id)

                if (umkm == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }

                // 2. Ambil Menu dan Services
                val menuList = when (umkm.category) {
                    "Makanan", "Minuman" -> repository.getUmkmMenu(id)
                    else -> emptyList()
                }

                val serviceList = when (umkm.category) {
                    "Jasa", "Kerajinan", "Fashion" -> repository.getUmkmServices(id)
                    else -> emptyList()
                }

                // 3. Ambil Reviews
                val reviewsList = repository.getReviewsByUmkmId(id)

                // 4. Update UI State
                _uiState.value = _uiState.value.copy(
                    umkm = umkm,
                    menuItems = menuList,
                    serviceItems = serviceList,
                    reviews = reviewsList,    // MASUKKAN KE STATE
                    isLoading = false
                )

            } catch (e: Exception) {
                println("ERROR fetching UMKM detail for $id: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    companion object {
        fun Factory(umkmId: String) = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DetailViewModel(umkmId) as T
            }
        }
    }
}
