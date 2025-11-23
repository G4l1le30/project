package com.example.umkami

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.umkami.ui.screens.CartScreen
import com.example.umkami.ui.screens.DetailScreen
import com.example.umkami.ui.screens.HomeScreen
import com.example.umkami.ui.screens.LoginScreen
import com.example.umkami.ui.screens.RegisterScreen
import com.example.umkami.ui.screens.ProfileScreen
import com.example.umkami.ui.screens.SplashScreen
import com.example.umkami.ui.screens.OrderHistoryScreen
import com.example.umkami.ui.screens.AddressScreen // Import AddressScreen // Import OrderHistoryScreen
import com.example.umkami.ui.theme.UmkamiTheme
import com.example.umkami.viewmodel.AuthViewModel
import com.example.umkami.viewmodel.CartViewModel
import com.example.umkami.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            // Create ViewModels that will be shared across composables
            val homeVm: HomeViewModel = viewModel()
            val cartVm: CartViewModel = viewModel()
            val authVm: AuthViewModel = viewModel() // Instantiate AuthViewModel

            val isAuthenticated by authVm.isAuthenticated.collectAsState()

            UmkamiTheme {
                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(navController = navController)
                        LaunchedEffect(isAuthenticated) {
                            if (isAuthenticated) {
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            } else {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }
                    }

                    composable("login") {
                        LoginScreen(navController = navController, authViewModel = authVm)
                    }

                    composable("register") {
                        RegisterScreen(navController = navController, authViewModel = authVm)
                    }

                    composable("profile") {
                        ProfileScreen(navController = navController, authViewModel = authVm)
                    }

                    composable("orderHistory") {
                        OrderHistoryScreen(navController = navController, authViewModel = authVm)
                    }

                    composable("address") {
                        AddressScreen(navController = navController, authViewModel = authVm)
                    }

                    composable("home") {
                        HomeScreen(
                            navController = navController, // Pass navController
                            homeVm = homeVm,
                            authVm = authVm, // Pass AuthViewModel
                            onUmkmClick = { umkmId ->
                                navController.navigate("detail/$umkmId")
                            },
                            onCartClick = {
                                navController.navigate("cart")
                            }
                        )
                    }

                    composable("detail/{umkmId}") { backStackEntry ->
                        val umkmId = backStackEntry.arguments?.getString("umkmId")
                        DetailScreen(
                            umkmId = umkmId,
                            navController = navController,
                            cartViewModel = cartVm, // Pass shared CartViewModel
                            authViewModel = authVm, // Pass shared AuthViewModel
                            homeViewModel = homeVm, // Pass shared HomeViewModel
                            onCartClick = {
                                navController.navigate("cart")
                            }
                        )
                    }

                    composable("cart") {
                        CartScreen(
                            navController = navController,
                            cartViewModel = cartVm, // Pass shared CartViewModel
                            authViewModel = authVm // Pass shared AuthViewModel
                        )
                    }
                }
            }
        }
    }
}