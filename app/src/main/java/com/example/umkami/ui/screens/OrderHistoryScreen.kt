package com.example.umkami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.umkami.data.model.Order
import com.example.umkami.viewmodel.AuthViewModel
import com.example.umkami.viewmodel.OrderHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderHistoryScreen(
    authViewModel: AuthViewModel,
    orderHistoryViewModel: OrderHistoryViewModel = viewModel()
) {
    val uiState by orderHistoryViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            orderHistoryViewModel.loadOrderHistory(user.uid)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
                Text(
                    text = "Error: ${uiState.error}",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            uiState.orders.isEmpty() -> {
                Text(
                    text = "Anda belum memiliki riwayat pesanan.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.orders) { order ->
                        OrderItem(order = order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pesanan dari: ${order.items.firstOrNull()?.umkmName ?: "UMKM"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tanggal: ${formatTimestamp(order.orderTimestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            order.items.forEach { cartItem ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${cartItem.quantity}x ${cartItem.item.name}")
                    Text(text = "Rp ${"%,d".format(cartItem.item.price * cartItem.quantity)}")
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total: Rp ${"%,d".format(order.totalPrice.toInt())}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
