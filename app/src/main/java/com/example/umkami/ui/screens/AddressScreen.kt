package com.example.umkami.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.umkami.ui.components.GradientButton
import com.example.umkami.viewmodel.AuthViewModel

@Composable
fun AddressScreen(
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var addressText by remember { mutableStateOf(currentUser?.address ?: "") }

    LaunchedEffect(currentUser) {
        addressText = currentUser?.address ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        OutlinedTextField(
            value = addressText,
            onValueChange = { addressText = it },
            label = { Text("Alamat") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        GradientButton(
            onClick = {
                if (currentUser != null) {
                    authViewModel.updateUserAddress(addressText)
                    Toast.makeText(context, "Alamat berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Tidak ada pengguna yang login.", Toast.LENGTH_SHORT).show()
                }
            },
            text = "Simpan Alamat",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Alamat saat ini: ${currentUser?.address ?: "Belum diatur"}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
