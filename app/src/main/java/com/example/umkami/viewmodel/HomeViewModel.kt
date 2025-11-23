package com.example.umkami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.Umkm
import com.example.umkami.data.repository.UmkmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map // Import ini diperlukan
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted // Import ini diperlukan
import kotlinx.coroutines.flow.stateIn // Import ini diperlukan
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = UmkmRepository()

    // Original list of UMKM fetched from Firebase
    private val _originalUmkmList = MutableStateFlow<List<Umkm>>(emptyList())

    // Backing property for the filtered list
    private val _filteredUmkmList = MutableStateFlow<List<Umkm>>(emptyList())
    // Publicly exposed immutable StateFlow for the UI to observe
    val filteredUmkmList: StateFlow<List<Umkm>> = _filteredUmkmList.asStateFlow()

    // State for search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // State for selected category filter
    private val _selectedCategory = MutableStateFlow("All") // Default to "All"
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Categories list is now a StateFlow derived from the original list
    val categories: StateFlow<List<String>> = _originalUmkmList.map { umkmList ->
        val distinctCategories = umkmList.map { it.category }.distinct().toMutableList()
        if (!distinctCategories.contains("All")) { // Ensure "All" is always first and not duplicated
            distinctCategories.add(0, "All")
        }
        distinctCategories
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("All") // Initial value for categories
    )

    init {
        loadUmkmList()
    }

    private fun loadUmkmList() {
        viewModelScope.launch {
            try {
                val list = repository.getUmkmFromFirebase()
                _originalUmkmList.value = list
                // Initially, the filtered list is the same as the original
                _filteredUmkmList.value = list
                // No need to call applyFilters here, as _filteredUmkmList is directly set
                // and categories flow will update reactively from _originalUmkmList update.
            } catch (e: Exception) {
                println("ERROR fetching UMKM list: ${e.message}")
                _originalUmkmList.value = emptyList()
                _filteredUmkmList.value = emptyList()
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters() // Call applyFilters after changing a filter state
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
        applyFilters() // Call applyFilters after changing a filter state
    }

    private fun applyFilters() { // Definisi fungsi applyFilters
        val query = _searchQuery.value
        val category = _selectedCategory.value

        _filteredUmkmList.value = _originalUmkmList.value.filter { umkm ->
            val matchesSearch = umkm.name.contains(query, ignoreCase = true) ||
                                umkm.description.contains(query, ignoreCase = true) ||
                                umkm.address.contains(query, ignoreCase = true)
            val matchesCategory = if (category == "All") true else umkm.category == category
            matchesSearch && matchesCategory
        }
    }
}
