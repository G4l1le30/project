package com.example.umkami.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.MenuItem
import com.example.umkami.data.model.Review
import com.example.umkami.data.model.ServiceItem
import com.example.umkami.data.model.Umkm
import com.example.umkami.data.repository.UmkmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// UI State for the Detail Screen
data class DetailUiState(
    val umkm: Umkm? = null,
    val menu: List<MenuItem> = emptyList(),
    val services: List<ServiceItem> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val reviewSubmissionSuccess: Boolean = false
)

class DetailViewModel : ViewModel() {

    private val repository = UmkmRepository()

    // Expose screen UI state
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _isWishlisted = MutableStateFlow(false)
    val isWishlisted: StateFlow<Boolean> = _isWishlisted.asStateFlow()

    // State for the review form
    var reviewAuthor by mutableStateOf("")
    var reviewComment by mutableStateOf("")
    var reviewRating by mutableStateOf(0)


    suspend fun loadUmkmDetails(umkmId: String): Umkm? {
        _uiState.value = _uiState.value.copy(isLoading = true)
        return try {
            val umkmDetails = repository.getUmkmById(umkmId)
            if (umkmDetails != null) {
                val menuItems = repository.getUmkmMenu(umkmId)
                val serviceItems = repository.getUmkmServices(umkmId)
                val reviews = repository.getReviewsByUmkmId(umkmId)

                _uiState.value = DetailUiState(
                    umkm = umkmDetails,
                    menu = menuItems,
                    services = serviceItems,
                    reviews = reviews,
                    isLoading = false
                )
                umkmDetails // Return the loaded UMKM
            } else {
                _uiState.value = DetailUiState(error = "UMKM not found.", isLoading = false)
                null // Return null if not found
            }
        } catch (e: Exception) {
            _uiState.value = DetailUiState(error = "Failed to load data: ${e.message}", isLoading = false)
            null // Return null on error
        }
    }

    fun submitReview(umkmId: String) {
        if (reviewComment.isBlank() || reviewRating == 0) {
            // Optional: Handle validation error in UI
            return
        }

        val authorName = if (reviewAuthor.isBlank()) "Anonymous" else reviewAuthor

        val newReview = Review(
            author = authorName,
            comment = reviewComment,
            rating = reviewRating.toFloat()
        )

        viewModelScope.launch {
            val success = repository.addReview(umkmId, newReview)
            if (success) {
                // Refresh reviews and clear the form
                // This call should ideally be to a non-suspending loadUmkmDetails or
                // ensure the context is handled. For now, it's fine in launch.
                loadUmkmDetails(umkmId) 
                reviewAuthor = ""
                reviewComment = ""
                reviewRating = 0
                _uiState.value = _uiState.value.copy(reviewSubmissionSuccess = true)
            } else {
                // Optional: show an error message in the UI
            }
        }
    }

    fun checkIfWishlisted(userId: String, umkmId: String) {
        viewModelScope.launch {
            _isWishlisted.value = repository.isWishlisted(userId, umkmId)
        }
    }

    fun addToWishlist(userId: String, umkmId: String) {
        viewModelScope.launch {
            repository.addToWishlist(userId, umkmId)
            _isWishlisted.value = true
        }
    }

    fun removeFromWishlist(userId: String, umkmId: String) {
        viewModelScope.launch {
            repository.removeFromWishlist(userId, umkmId)
            _isWishlisted.value = false
        }
    }

    fun resetSubmissionStatus() {
        _uiState.value = _uiState.value.copy(reviewSubmissionSuccess = false)
    }
}