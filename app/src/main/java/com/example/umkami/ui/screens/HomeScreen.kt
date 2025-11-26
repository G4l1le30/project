package com.example.umkami.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.umkami.data.model.Umkm
import com.example.umkami.viewmodel.AuthViewModel
import com.example.umkami.viewmodel.HomeViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onUmkmClick: (String) -> Unit,
    homeVm: HomeViewModel = viewModel(),
    authVm: AuthViewModel = viewModel()
) {
    var isMapMode by remember { mutableStateOf(false) }

    val filteredUmkmList by homeVm.filteredUmkmList.collectAsState()
    val searchQuery by homeVm.searchQuery.collectAsState()
    val selectedCategory by homeVm.selectedCategory.collectAsState()
    val allCategories by homeVm.categories.collectAsState()
    val recommendedUmkmList by homeVm.recommendedUmkmList.collectAsState()
    val currentUser by authVm.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            homeVm.loadRecommendedUmkm(user.uid)
        } else {
            homeVm.loadRecommendedUmkm("")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isMapMode) {
            val malang = LatLng(-7.96, 112.63)

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(malang, 12f)
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                filteredUmkmList.forEach { umkm ->
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { homeVm.setSearchQuery(it) },
                    label = { Text("Search UMKM") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(allCategories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { homeVm.setSelectedCategory(category) },
                            label = { Text(category) }
                        )
                    }
                }

                if (recommendedUmkmList.isNotEmpty()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = "Rekomendasi Untuk Anda",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(recommendedUmkmList) { umkm ->
                                RecommendedUmkmItem(umkm, onUmkmClick)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (filteredUmkmList.isEmpty() && searchQuery.isNotEmpty()) {
                        item {
                            Text(
                                text = "No UMKM found for \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (filteredUmkmList.isEmpty() && selectedCategory != "All") {
                        item {
                            Text(
                                text = "No UMKM found in category \"$selectedCategory\"",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (filteredUmkmList.isEmpty()) {
                         item {
                            Text(
                                text = "No UMKM available.",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    items(filteredUmkmList) { umkm ->
                        UmkmItem(umkm, onUmkmClick)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { isMapMode = !isMapMode },
            modifier = Modifier
                .align(alignment = androidx.compose.ui.Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (isMapMode) Icons.AutoMirrored.Filled.List else Icons.Default.Map,
                contentDescription = null
            )
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
                    .height(200.dp),
                contentScale = ContentScale.Crop
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendedUmkmItem(umkm: Umkm, onUmkmClick: (String) -> Unit) {
    Card(
        onClick = { onUmkmClick(umkm.id) },
        modifier = Modifier.width(180.dp),
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
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                Text(
                    text = umkm.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = umkm.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
