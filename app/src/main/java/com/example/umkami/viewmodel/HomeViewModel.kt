package com.example.umkami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.Umkm
import com.example.umkami.data.repository.UmkmRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {

    private val repository = UmkmRepository()
    private val database: FirebaseDatabase = Firebase.database
    private val userPreferencesRef = database.getReference("user_preferences")
    private var homeScreenRecommendationsLoaded = false

    private val _originalUmkmList = MutableStateFlow<List<Umkm>>(emptyList())

    private val _filteredUmkmList = MutableStateFlow<List<Umkm>>(emptyList())
    val filteredUmkmList: StateFlow<List<Umkm>> = _filteredUmkmList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _recommendedUmkmList = MutableStateFlow<List<Umkm>>(emptyList())
    val recommendedUmkmList: StateFlow<List<Umkm>> = _recommendedUmkmList.asStateFlow()

    val categories: StateFlow<List<String>> = _originalUmkmList.map { umkmList ->
        val distinctCategories = umkmList.map { it.category }.distinct().toMutableList()
        if (!distinctCategories.contains("All")) {
            distinctCategories.add(0, "All")
        }
        distinctCategories
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("All")
    )

    init {
        loadUmkmList()
    }

    private fun loadUmkmList() {
        viewModelScope.launch {
            try {
                val list = repository.getUmkmFromFirebase()
                _originalUmkmList.value = list
                _filteredUmkmList.value = list
            } catch (e: Exception) {
                println("ERROR fetching UMKM list: ${e.message}")
                _originalUmkmList.value = emptyList()
                _filteredUmkmList.value = emptyList()
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
        applyFilters()
    }

    private fun applyFilters() {
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

    fun onHomeScreenReady(uid: String?) {
        if (homeScreenRecommendationsLoaded) return
        homeScreenRecommendationsLoaded = true
        loadRecommendedUmkm(uid ?: "")
    }

    fun recordCategoryView(uid: String, category: String) {
        if (uid.isNotBlank() && category.isNotBlank() && category != "All") {
            viewModelScope.launch {
                val categoryPath = userPreferencesRef.child(uid).child("viewed_categories").child(category)
                try {
                    val currentCount = categoryPath.child("count").get().await().getValue(Int::class.java) ?: 0
                    categoryPath.child("count").setValue(currentCount + 1).await()
                    categoryPath.child("last_viewed").setValue(System.currentTimeMillis()).await()
                    println("User $uid viewed category $category. Count: ${currentCount + 1}")
                } catch (e: Exception) {
                    println("Error recording category view: ${e.message}")
                }
            }
        }
    }

    fun loadRecommendedUmkm(uid: String) {
        viewModelScope.launch {
            if (uid.isBlank()) {
                _recommendedUmkmList.value = _originalUmkmList.value.shuffled().take(3)
                return@launch
            }

            try {
                val snapshot = userPreferencesRef.child(uid).child("viewed_categories").get().await()
                val viewedCategories = mutableMapOf<String, Int>()

                snapshot.children.forEach { categorySnapshot ->
                    val categoryName = categorySnapshot.key
                    val count = categorySnapshot.child("count").getValue(Int::class.java)
                    if (categoryName != null && count != null) {
                        viewedCategories[categoryName] = count
                    }
                }

                if (viewedCategories.isEmpty()) {
                    _recommendedUmkmList.value = _originalUmkmList.value.shuffled().take(3)
                } else {
                    val topCategories = viewedCategories.entries.sortedByDescending { it.value }.map { it.key }

                    val recommendations = mutableListOf<Umkm>()
                    val processedUmkmIds = mutableSetOf<String>()

                    topCategories.forEach { category ->
                        _originalUmkmList.value
                            .filter { umkm -> umkm.category == category && umkm.id !in processedUmkmIds }
                            .shuffled()
                            .take(2)
                            .let {
                                recommendations.addAll(it)
                                processedUmkmIds.addAll(it.map { u -> u.id })
                            }
                    }

                    if (recommendations.size < 5) {
                        _originalUmkmList.value
                            .filter { it.id !in processedUmkmIds }
                            .shuffled()
                            .take(5 - recommendations.size)
                            .let { recommendations.addAll(it) }
                    }
                    _recommendedUmkmList.value = recommendations.take(5)
                }

            } catch (e: Exception) {
                println("Error loading recommended UMKM: ${e.message}")
                _recommendedUmkmList.value = emptyList()
            }
        }
    }
}