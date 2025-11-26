package com.example.umkami.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import androidx.navigation.NavController
import com.example.umkami.data.model.CartItem
import com.example.umkami.viewmodel.AuthViewModel
import com.example.umkami.viewmodel.CartViewModel

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val groupedCartItems by cartViewModel.groupedCartItems.collectAsState()
    val totalPrice by cartViewModel.totalPrice.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        if (groupedCartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                groupedCartItems.forEach { (umkmName, items) ->
                    item {
                        Text(
                            text = "Pesanan dari: $umkmName",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
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

        if (groupedCartItems.isNotEmpty()) {
            BottomAppBar(
                modifier = Modifier.align(Alignment.BottomCenter),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(text = cartItem.item.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Rp ${"%,d".format(cartItem.item.price)}", color = MaterialTheme.colorScheme.secondary)
            }

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