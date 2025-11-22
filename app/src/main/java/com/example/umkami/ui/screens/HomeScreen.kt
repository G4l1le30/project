package com.example.umkami.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.umkami.data.model.Umkm
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    umkmList: List<Umkm>,
    onUmkmClick: (String) -> Unit
) {
    // Mode tampilan: List atau Map
    var isMapMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isMapMode) "UMKami Map View" else "UMKami Listings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isMapMode = !isMapMode }) {
                Icon(
                    imageVector = if (isMapMode) Icons.Default.List else Icons.Default.Map,
                    contentDescription = null
                )
            }
        }
    ) { paddingValues ->

        if (isMapMode) {
            // ------------ MAP MODE ------------
            val malang = LatLng(-7.96, 112.63)

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(malang, 12f)
            }

            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                cameraPositionState = cameraPositionState
            ) {
                umkmList.forEach { umkm ->
                    Marker(
                        state = MarkerState(position = LatLng(umkm.lat, umkm.lng)),
                        title = umkm.name,
                        snippet = umkm.address,
                        onClick = {
                            Log.d("Map", "Clicked on ${umkm.name}")
                            onUmkmClick(umkm.id)
                            true
                        }
                    )
                }
            }
        } else {
            // ------------ LIST MODE ------------
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(umkmList) { umkm ->
                    UmkmItem(umkm, onUmkmClick)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UmkmItem(umkm: Umkm, onUmkmClick: (String) -> Unit) {
    Card(
        onClick = { onUmkmClick(umkm.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = umkm.imageUrl,
                contentDescription = umkm.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = umkm.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = umkm.category,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = umkm.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
