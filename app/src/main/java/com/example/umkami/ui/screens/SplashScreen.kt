package com.example.umkami.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.umkami.R // Pastikan import R benar
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // LaunchedEffect akan berjalan sekali saat composable ini pertama kali masuk ke komposisi
    LaunchedEffect(key1 = true) {
        // Delay selama 2 detik (sesuaikan durasi sesuai keinginan Anda)
        delay(2000L)
        // Setelah delay, navigasi ke layar 'home' dan hapus SplashScreen dari back stack
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true } // Hapus splash dari stack
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer), // Gunakan warna tema Anda
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Tampilkan logo UMKami Anda
        Image(
            painter = painterResource(id = R.mipmap.ic_umkami_logo), // Anda akan menambahkan ini
            contentDescription = "UMKami Logo",
            modifier = Modifier.size(200.dp) // Ukuran logo
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "UMKami",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Temukan UMKM Favoritmu!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}