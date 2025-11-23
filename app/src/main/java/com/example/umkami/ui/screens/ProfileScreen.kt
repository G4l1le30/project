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
import androidx.lifecycle.viewmodel.compose.viewModel // Add this import
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            // If logged out, navigate back to login screen
            navController.navigate("login") {
                popUpTo("home") { inclusive = true } // Clear back stack up to home
            }
            Toast.makeText(context, "Logout successful!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profil Pengguna") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (currentUser != null) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Icon",
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Email: ${currentUser?.email}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nama: ${currentUser?.displayName}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Order History Button
                GradientButton(
                    onClick = { navController.navigate("orderHistory") },
                    text = "Riwayat Pesanan",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Address Button
                GradientButton(
                    onClick = { navController.navigate("address") },
                    text = "Alamat Saya",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // My Cart Button
                GradientButton(
                    onClick = { navController.navigate("cart") },
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
                Text(
                    text = "Anda belum login.",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("login") }) {
                    Text("Login Sekarang")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    UmkamiTheme {
        ProfileScreen(navController = NavController(LocalContext.current), authViewModel = viewModel())
    }
}