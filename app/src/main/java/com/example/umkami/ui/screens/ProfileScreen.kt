package com.example.umkami.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.umkami.ui.components.GradientButton
import com.example.umkami.ui.theme.UmkamiTheme
import com.example.umkami.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateToOrderHistory: () -> Unit,
    onNavigateToAddress: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isAuthenticated) {
            if (currentUser != null) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Icon",
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Email: ${currentUser?.email ?: "Data tidak tersedia"}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nama: ${currentUser?.displayName ?: "Data tidak tersedia"}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Saldo",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Saldo Anda",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Rp ${"%,.0f".format(currentUser?.balance ?: 0.0)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Top Up Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Top Up Saldo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val isLoading by authViewModel.isLoading.collectAsState()

                        val topUpAmounts = listOf(25000, 50000, 100000)
                        topUpAmounts.forEach { amount ->
                            OutlinedButton(
                                onClick = {
                                    authViewModel.topUpBalance(amount.toDouble())
                                    Toast.makeText(context, "Top up Rp $amount sedang diproses...", Toast.LENGTH_SHORT).show()
                                },
                                enabled = !isLoading
                            ) {
                                Text("Rp ${"%,d".format(amount)}")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                GradientButton(
                    onClick = onNavigateToOrderHistory,
                    text = "Riwayat Pesanan",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                GradientButton(
                    onClick = onNavigateToAddress,
                    text = "Alamat Saya",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                GradientButton(
                    onClick = onNavigateToCart,
                    text = "Keranjang Saya",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                GradientButton(
                    onClick = { authViewModel.logout() },
                    text = "Logout",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Authenticated, but user data is still loading
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Memuat data pengguna...")
            }
        } else {
            Text(
                text = "Anda belum login.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToLogin) {
                Text("Login Sekarang")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    UmkamiTheme {
        ProfileScreen(
            authViewModel = viewModel(),
            onNavigateToOrderHistory = {},
            onNavigateToAddress = {},
            onNavigateToCart = {},
            onNavigateToLogin = {}
        )
    }
}