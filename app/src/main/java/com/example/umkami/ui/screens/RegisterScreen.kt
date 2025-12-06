package com.example.umkami.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel // Add this import
import com.example.umkami.ui.components.GradientButton
import com.example.umkami.ui.theme.UmkamiTheme
import com.example.umkami.viewmodel.AuthViewModel // Will create this later

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel // Will be injected via Hilt or custom provider
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("customer") } // "customer" or "owner"

    val context = LocalContext.current
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val isRegistered by authViewModel.isRegistered.collectAsState()

    // Handle registration success
    LaunchedEffect(isRegistered) {
        if (isRegistered) {
            Toast.makeText(context, "Registration Successful! Please login.", Toast.LENGTH_LONG).show()
            navController.popBackStack() // Go back to login screen
            authViewModel.clearRegisteredFlag()
        }
    }

    // Handle registration errors
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Daftar Akun Baru",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Konfirmasi Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm Password Icon") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Role Selection
            Column(Modifier.fillMaxWidth()) {
                Text("Daftar sebagai:", style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = role == "customer",
                        onClick = { role = "customer" }
                    )
                    Text("Pembeli")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = role == "owner",
                        onClick = { role = "owner" }
                    )
                    Text("Pemilik UMKM")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                GradientButton(
                    onClick = {
                        if (password == confirmPassword) {
                            if(displayName.isNotBlank()) {
                                authViewModel.register(email, password, displayName, role)
                            } else {
                                Toast.makeText(context, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Password tidak cocok!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    text = "Daftar",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Sudah punya akun? Login di sini")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    UmkamiTheme {
        RegisterScreen(navController = rememberNavController(), authViewModel = viewModel()) // Preview with dummy ViewModel
    }
}