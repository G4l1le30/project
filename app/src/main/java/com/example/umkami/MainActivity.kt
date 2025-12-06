package com.example.umkami

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.umkami.ui.components.BottomNavigationBar
import com.example.umkami.ui.components.UmkamiTopAppBar
import com.example.umkami.ui.screens.CartScreen
import com.example.umkami.ui.screens.DetailScreen
import com.example.umkami.ui.screens.HomeScreen
import com.example.umkami.ui.screens.LoginScreen
import com.example.umkami.ui.screens.RegisterScreen
import com.example.umkami.ui.screens.ProfileScreen
import com.example.umkami.ui.screens.SplashScreen
import com.example.umkami.ui.screens.OrderHistoryScreen
import com.example.umkami.ui.screens.AddressScreen
import com.example.umkami.ui.screens.WishlistScreen
import com.example.umkami.ui.screens.OwnerDashboardScreen
import com.example.umkami.ui.theme.UmkamiTheme
import com.example.umkami.viewmodel.AuthViewModel
import com.example.umkami.viewmodel.CartViewModel
import com.example.umkami.viewmodel.HomeViewModel
import com.example.umkami.viewmodel.OwnerDashboardViewModel
import com.example.umkami.viewmodel.WishlistViewModel

import androidx.activity.enableEdgeToEdge
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            // Create ViewModels that will be shared across composables
            val homeVm: HomeViewModel = viewModel()
            val cartVm: CartViewModel = viewModel()
            val authVm: AuthViewModel = viewModel()
            val wishlistVm: WishlistViewModel = viewModel()
            val ownerDashboardVm: OwnerDashboardViewModel = viewModel()

            val isAuthenticated by authVm.isAuthenticated.collectAsState()
            val currentUser by authVm.currentUser.collectAsState()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            UmkamiTheme {
                LaunchedEffect(isAuthenticated, currentUser) {
                    if (isAuthenticated) {
                        val user = currentUser
                        if (user != null) {
                            if (user.role == "owner") {
                                navController.navigate("owner_dashboard") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            } else {
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }
                    } else {
                        // Check if the current route is not 'login' or 'register' to avoid navigation loops
                        if (currentRoute != "login" && currentRoute != "register") {
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
                Scaffold(
                    topBar = {
                        if (currentRoute in listOf("home", "wishlist", "cart", "orderHistory", "profile", "address")) {
                            UmkamiTopAppBar(title = currentRoute?.replaceFirstChar { it.uppercase() } ?: "")
                        }
                    },
                    bottomBar = {
                        if (currentRoute in listOf("home", "wishlist", "cart", "orderHistory", "profile", "address")) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splash") {
                            SplashScreen(navController = navController)
                        }

                        composable("login") {
                            LoginScreen(navController = navController, authViewModel = authVm)
                        }

                        composable("register") {
                            RegisterScreen(navController = navController, authViewModel = authVm)
                        }

                        composable("profile") {
                            ProfileScreen(
                                authViewModel = authVm,
                                onNavigateToOrderHistory = { navController.navigate("orderHistory") },
                                onNavigateToAddress = { navController.navigate("address") },
                                onNavigateToCart = { navController.navigate("cart") },
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("orderHistory") {
                            OrderHistoryScreen(authViewModel = authVm)
                        }

                        composable("address") {
                            AddressScreen(authViewModel = authVm)
                        }

                        composable("home") {
                            HomeScreen(
                                homeVm = homeVm,
                                authVm = authVm,
                                onUmkmClick = { umkmId ->
                                    navController.navigate("detail/$umkmId")
                                }
                            )
                        }

                        composable("detail/{umkmId}") { backStackEntry ->
                            val umkmId = backStackEntry.arguments?.getString("umkmId")
                            DetailScreen(
                                umkmId = umkmId,
                                navController = navController,
                                cartViewModel = cartVm,
                                authViewModel = authVm,
                                homeViewModel = homeVm,
                                onCartClick = {
                                    navController.navigate("cart")
                                }
                            )
                        }

                        composable("cart") {
                            CartScreen(
                                cartViewModel = cartVm,
                                authViewModel = authVm,
                                navController = navController
                            )
                        }

                        composable("wishlist") {
                            WishlistScreen(
                                authViewModel = authVm,
                                wishlistViewModel = wishlistVm,
                                onUmkmClick = { umkmId ->
                                    navController.navigate("detail/$umkmId")
                                }
                            )
                        }

                        composable("owner_dashboard") {
                            OwnerDashboardScreen(
                                navController = navController,
                                authViewModel = authVm,
                                ownerDashboardViewModel = ownerDashboardVm
                            )
                        }
                    }
                }
            }
        }
    }
}