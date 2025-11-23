package com.example.umkami

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.umkami.ui.screens.CartScreen
import com.example.umkami.ui.screens.DetailScreen
import com.example.umkami.ui.screens.HomeScreen
import com.example.umkami.ui.screens.SplashScreen
import com.example.umkami.ui.theme.UmkamiTheme
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

            UmkamiTheme {
                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(navController = navController)
                    }

                    composable("home") {
                        HomeScreen(
                            homeVm = homeVm,
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
                            onCartClick = {
                                navController.navigate("cart")
                            }
                        )
                    }

                    composable("cart") {
                        CartScreen(
                            navController = navController,
                            cartViewModel = cartVm // Pass shared CartViewModel
                        )
                    }
                }
            }
        }
    }
}