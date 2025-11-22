package com.example.umkami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.umkami.data.model.Umkm
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(umkmId: String?, navController: NavController) {
    // Implementasi sederhana untuk tujuan UI (membutuhkan logika pencarian data nyata)
    // Asumsi: Kita membuat objek UMKM sementara (mock) untuk demonstrasi
    val umkmDetail = Umkm(
        id = umkmId ?: "error",
        name = "Bakso Mantap (ID: $umkmId)",
        description = "Bakso enak, gurih, dan kenyal. Dibuat dengan daging sapi pilihan. Cocok untuk semua kalangan.",
        category = "Makanan",
        address = "Jl. Veteran, Malang",
        imageUrl = "https://raw.githubusercontent.com/G4l1le30/project/master/assets/images/umkm0/Bakso_mi_bihun.jpg"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(umkmDetail.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Gambar Besar (Sesuai UI Kit)
            AsyncImage(
                model = umkmDetail.imageUrl,
                contentDescription = umkmDetail.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(bottom = 16.dp)
            )

            // Deskripsi UMKM
            Text(
                text = "Description:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = umkmDetail.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Kategori dan Alamat
            Text(text = "Category: ${umkmDetail.category}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Address: ${umkmDetail.address}", style = MaterialTheme.typography.bodyMedium)

            // TODO: Tambahkan tombol "Lihat di Peta" di sini untuk meniru Map View di UI Kit
        }
    }
}