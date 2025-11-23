package com.example.umkami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.Order
import com.example.umkami.data.repository.UmkmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrderHistoryUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class OrderHistoryViewModel : ViewModel() {

    private val repository = UmkmRepository()

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    fun loadOrderHistory(userId: String) {
        if (userId.isBlank()) {
            _uiState.value = OrderHistoryUiState(isLoading = false, error = "User not logged in.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val orders = repository.getOrdersByUserId(userId)
                _uiState.value = OrderHistoryUiState(orders = orders, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = OrderHistoryUiState(error = "Failed to load order history: ${e.message}", isLoading = false)
            }
        }
    }
}
