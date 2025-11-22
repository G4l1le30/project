package com.example.umkami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.umkami.data.model.Umkm
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// WAJIB: Tambahkan onUmkmClick untuk navigasi
fun HomeScreen(umkmList: List<Umkm>, onUmkmClick: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UMKami Listings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    // Warna bersih dan konsisten
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(umkmList) { umkm ->
                // Panggil UmkmItem dengan fungsi klik
                UmkmItem(umkm, onUmkmClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// WAJIB: Tambahkan onUmkmClick untuk navigasi
fun UmkmItem(umkm: Umkm, onUmkmClick: (String) -> Unit) {
    Card(
        // PENTING: Memicu navigasi menggunakan ID UMKM
        onClick = { onUmkmClick(umkm.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // Bentuk sudut yang lembut
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Minimalis
    ) {
        Column {
            // 1. Gambar
            AsyncImage(
                model = umkm.imageUrl,
                contentDescription = umkm.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Lebih tinggi
            )

            // 2. Konten Teks
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

                // Nama UMKM
                Text(
                    text = umkm.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Kategori
                Text(
                    text = umkm.category,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary, // Warna Aksen
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Alamat
                Text(
                    text = umkm.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}