// ui/screens/HomeScreen.kt

package com.example.umkami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.* // Ubah import untuk Material3
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.umkami.data.model.Umkm
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(umkmList: List<Umkm>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UMKM Listings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(umkmList) { umkm ->
                UmkmItem(umkm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UmkmItem(umkm: Umkm) {
    Card(
        onClick = { /* TODO: Navigasi ke detail UMKM */ },
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow // Warna yang lebih menarik
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Tambahkan sedikit elevasi
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            AsyncImage(
                model = umkm.imageUrl,
                contentDescription = umkm.name,
                contentScale = ContentScale.Crop, // Crop gambar agar mengisi area
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nama UMKM (Lebih menonjol)
            Text(
                text = umkm.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Kategori (Warna aksen)
            Text(
                text = umkm.category,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary // Gunakan warna primary untuk aksen
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Alamat
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Untuk UI minimalis, bisa ditambahkan ikon (tapi tidak wajib)
                Text(
                    text = umkm.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}