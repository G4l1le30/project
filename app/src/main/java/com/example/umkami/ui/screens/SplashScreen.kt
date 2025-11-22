package com.example.umkami.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.umkami.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Durasi tampil Splash Screen (misal: 1.5 detik)
    val SPLASH_DURATION = 1500L

    LaunchedEffect(key1 = true) {
        delay(SPLASH_DURATION)
        // Navigasi ke layar 'home' dan hapus SplashScreen dari stack
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Menggunakan warna Surface
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Tampilkan logo UMKami Anda
        Image(
            // PERBAIKAN KRITIS: Merujuk ke foreground layer
            painter = painterResource(id = R.mipmap.ic_umkami_logo_foreground),
            contentDescription = "UMKami Logo",
            modifier = Modifier.size(250.dp) // Ukuran logo lebih besar
        )
        Spacer(modifier = Modifier.height(24.dp))

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Temukan UMKM Favoritmu!",
            style = MaterialTheme.typography.titleMedium, // Ukuran font sedang
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}