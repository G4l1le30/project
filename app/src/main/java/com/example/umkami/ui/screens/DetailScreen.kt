package com.example.umkami.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.umkami.data.model.Review
import com.example.umkami.viewmodel.AuthViewModel // Import AuthViewModel
import com.example.umkami.viewmodel.CartViewModel
import com.example.umkami.viewmodel.DetailViewModel
import com.example.umkami.viewmodel.HomeViewModel // Import HomeViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    umkmId: String?,
    navController: NavController,
    onCartClick: () -> Unit,
    detailViewModel: DetailViewModel = viewModel(),
    cartViewModel: CartViewModel, // Inject CartViewModel
    authViewModel: AuthViewModel, // Inject AuthViewModel
    homeViewModel: HomeViewModel // Inject HomeViewModel
) {
    val uiState by detailViewModel.uiState.collectAsState()
    val umkm = uiState.umkm
    val context = LocalContext.current
    val cartItemCount by cartViewModel.cartItems.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState() // Collect current user

    // Load data and record view when the screen is first composed
    LaunchedEffect(umkmId) {
        if (umkmId != null) {
            detailViewModel.loadUmkmDetails(umkmId)
        }
    }

    LaunchedEffect(umkm, currentUser) {
        val user = currentUser
        if (umkm != null && user != null) {
            homeViewModel.recordCategoryView(user.uid, umkm.category)
            homeViewModel.loadRecommendedUmkm(user.uid) // Trigger loading recommendations after recording view
        } else {
            homeViewModel.loadRecommendedUmkm("") // Load generic recommendations if no user
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(umkm?.name ?: "Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (cartItemCount.isNotEmpty()) {
                                Badge { Text(cartItemCount.size.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = onCartClick) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Shopping Cart"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}")
                }
            }
            umkm != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header Image
                    item {
                        AsyncImage(
                            model = umkm.imageUrl,
                            contentDescription = "Image of ${umkm.name}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Basic Info
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(umkm.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(umkm.category, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                            Text(umkm.description, style = MaterialTheme.typography.bodyLarge)
                            Text(umkm.address, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            // "Hubungi" button for service-based UMKMs
                            if (umkm.category == "Jasa" && umkm.contact.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Contact: ${umkm.contact}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${umkm.contact}")
                                    }
                                    context.startActivity(intent)
                                }) {
                                    Icon(Icons.Default.Call, contentDescription = "Hubungi UMKM")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Hubungi")
                                }
                            }
                        }
                    }

                    // Map View
                    item {
                        val location = LatLng(umkm.lat, umkm.lng)
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(location, 15f)
                        }
                        GoogleMap(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            cameraPositionState = cameraPositionState
                        ) {
                            Marker(state = MarkerState(position = location), title = umkm.name)
                        }
                    }

                    // Menu or Services
                    if (uiState.menu.isNotEmpty()) {
                        item {
                            SectionTitle("Menu")
                        }
                        items(uiState.menu) { menuItem ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = MaterialTheme.shapes.medium, // Apply rounded shape
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(menuItem.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "Rp ${"%,d".format(menuItem.price)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { cartViewModel.addItem(menuItem, umkm.name) }) {
                                        Icon(Icons.Default.Add, contentDescription = "Add to Cart")
                                    }
                                }
                            }
                        }                    } else if (uiState.services.isNotEmpty()) {
                        item {
                            SectionTitle("Services")
                        }
                        items(uiState.services) { serviceItem ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = MaterialTheme.shapes.medium, // Apply rounded shape
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(serviceItem.service, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "Rp ${"%,d".format(serviceItem.price)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }                    }

                    // Reviews Section
                    item {
                        SectionTitle("Reviews")
                    }
                    if (uiState.reviews.isEmpty()) {
                        item { Text("No reviews yet. Be the first to review!", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                    } else {
                        items(uiState.reviews) { review ->
                            ReviewItem(review)
                        }
                    }


                    // Add Review Form
                    item {
                        AddReviewForm(
                            author = detailViewModel.reviewAuthor,
                            onAuthorChange = { detailViewModel.reviewAuthor = it },
                            comment = detailViewModel.reviewComment,
                            onCommentChange = { detailViewModel.reviewComment = it },
                            rating = detailViewModel.reviewRating,
                            onRatingChange = { detailViewModel.reviewRating = it },
                            onSubmit = {
                                if (umkmId != null) {
                                    detailViewModel.submitReview(umkmId)
                                }
                            }
                        )
                    }

                    // Spacer at the bottom
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(review.author, fontWeight = FontWeight.Bold)
                StarRatingDisplay(rating = review.rating.toInt())
            }
            Spacer(Modifier.height(4.dp))
            Text(review.comment)
        }
    }
}

@Composable
fun AddReviewForm(
    author: String,
    onAuthorChange: (String) -> Unit,
    comment: String,
    onCommentChange: (String) -> Unit,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle("Leave a Review")

        OutlinedTextField(
            value = author,
            onValueChange = onAuthorChange,
            label = { Text("Your Name (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChange,
            label = { Text("Your Review") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        StarRatingInput(rating = rating, onRatingChange = onRatingChange)

        Button(
            onClick = onSubmit,
            modifier = Modifier.align(Alignment.End),
            enabled = comment.isNotBlank() && rating > 0
        ) {
            Text("Submit")
        }
    }
}

@Composable
fun StarRatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    maxRating: Int = 5
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Your Rating: ", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        for (i in 1..maxRating) {
            IconButton(onClick = { onRatingChange(i) }) {
                Icon(
                    imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Star $i",
                    tint = if (i <= rating) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun StarRatingDisplay(rating: Int, maxRating: Int = 5) {
    Row {
        for (i in 1..maxRating) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFFD700) else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}