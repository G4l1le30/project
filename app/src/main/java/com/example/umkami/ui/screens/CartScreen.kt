package com.example.umkami.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.umkami.data.model.CartItem
import com.example.umkami.viewmodel.AuthViewModel
import com.example.umkami.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel // Inject AuthViewModel
) {
    // Use groupedCartItems for multi-vendor display
    val groupedCartItems by cartViewModel.groupedCartItems.collectAsState()
    val totalPrice by cartViewModel.totalPrice.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cart") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (groupedCartItems.isNotEmpty()) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total: Rp ${"%,.0f".format(totalPrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                val uid = currentUser?.uid
                                if (uid != null) {
                                    cartViewModel.placeOrder(uid) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        } else {
                                            Toast.makeText(context, "Failed to place order.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "You must be logged in to place an order.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = groupedCartItems.isNotEmpty()
                        ) {
                            Text("Place Order")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (groupedCartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your cart is empty.",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                groupedCartItems.forEach { (umkmName, items) ->
                    // Header for each UMKM group
                    item {
                        Text(
                            text = "Pesanan dari: $umkmName",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    // Items for this UMKM
                    items(items) { cartItem ->
                        CartItemRow(
                            cartItem = cartItem,
                            onAddItem = { cartViewModel.addItem(cartItem.item, cartItem.umkmName) },
                            onRemoveItem = { cartViewModel.removeItem(cartItem.item, cartItem.umkmName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    cartItem: CartItem,
    onAddItem: () -> Unit,
    onRemoveItem: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Item Name and Price
            Column(modifier = Modifier.weight(1f)) {
                Text(text = cartItem.item.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Rp ${"%,d".format(cartItem.item.price)}", color = MaterialTheme.colorScheme.secondary)
            }

            // Quantity Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRemoveItem) {
                    Icon(Icons.Default.Remove, contentDescription = "Remove one")
                }
                Text(text = cartItem.quantity.toString(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = onAddItem) {
                    Icon(Icons.Default.Add, contentDescription = "Add one")
                }
            }
        }
    }
}