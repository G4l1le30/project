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
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel // Will be injected via Hilt or custom provider
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Handle authentication success
    LaunchedEffect(isAuthenticated, currentUser) {
        if (isAuthenticated) {
            val user = currentUser
            if (user != null) {
                if (user.role == "owner") {
                    navController.navigate("owner_dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle authentication errors
    LaunchedEffect(error) {
        error?.let {
            if (it.isNotEmpty() && it != "User data not found in database for UID: null") {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                authViewModel.clearError()
            }
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
                text = "Selamat Datang Kembali!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

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
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                GradientButton(
                    onClick = { authViewModel.login(email, password) },
                    text = "Login",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("register") }) {
                Text("Belum punya akun? Daftar di sini")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    UmkamiTheme {
        LoginScreen(navController = rememberNavController(), authViewModel = viewModel()) // Preview with dummy ViewModel
    }
}