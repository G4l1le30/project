package com.example.umkami.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.umkami.viewmodel.DetailViewModel

@Composable
fun DetailScreen(
    umkmId: String?,
    navController: NavController
) {
    val vm: DetailViewModel = viewModel(
        factory = DetailViewModel.Factory(umkmId ?: "")
    )

    val state = vm.uiState.collectAsState().value

    if (state.isLoading) {
        LoadingView()
        return
    }

    val umkm = state.umkm ?: return

    DetailContent(
        name = umkm.name,
        imageUrl = umkm.imageUrl,
        description = umkm.description,
        category = umkm.category,
        menuItems = state.menuItems.map { it.name },
        serviceItems = state.serviceItems.map { it.service },
        reviews = state.reviews,   // ⭐ Ambil review dinamiss dari Firebase
        onBack = { navController.popBackStack() }
    )
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun DetailContent(
    name: String,
    imageUrl: String,
    description: String,
    category: String,
    menuItems: List<String>,
    serviceItems: List<String>,
    reviews: List<String>,   // ⭐ Reviews dikirim dari DetailScreen
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Button(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (imageUrl.isNotBlank()) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = name, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Kategori: $category", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = description, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(20.dp))

        // MENU
        if (menuItems.isNotEmpty()) {
            Text("Menu:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            menuItems.forEach { Text(text = "- $it") }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // SERVICES
        if (serviceItems.isNotEmpty()) {
            Text("Services:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            serviceItems.forEach { Text(text = "- $it") }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ⭐ REVIEW DYNAMIC SECTION
        ReviewSection(reviews)
    }
}

@Composable
fun ReviewSection(reviews: List<String>) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "Ulasan Pengunjung",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (reviews.isEmpty()) {
            Text("Belum ada ulasan.", style = MaterialTheme.typography.bodyMedium)
            return
        }

        reviews.forEach { review ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = review,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
