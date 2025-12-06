package com.example.umkami.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.umkami.viewmodel.AuthViewModel
import com.example.umkami.viewmodel.WishlistViewModel

@Composable
fun WishlistScreen(
    authViewModel: AuthViewModel,
    wishlistViewModel: WishlistViewModel = viewModel(),
    onUmkmClick: (String) -> Unit
) {
    val wishlist by wishlistViewModel.wishlist.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            wishlistViewModel.loadWishlist(user.uid)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (wishlist.isEmpty()) {
            Text(
                text = "Your wishlist is empty.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(wishlist) { umkm ->
                    UmkmItem(umkm = umkm, onUmkmClick = onUmkmClick)
                }
            }
        }
    }
}
