package com.example.umkami.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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