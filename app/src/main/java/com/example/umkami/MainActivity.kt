package com.example.umkami

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.umkami.ui.screens.HomeScreen
import com.example.umkami.ui.screens.DetailScreen
import com.example.umkami.ui.screens.SplashScreen // WAJIB DIIMPORT
import com.example.umkami.ui.theme.UmkamiTheme
import com.example.umkami.viewmodel.HomeViewModel
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val homeVm: HomeViewModel = viewModel()
            val navController = rememberNavController()

            UmkamiTheme {
                NavHost(
                    navController = navController,
                    // PERBAIKAN: Set startDestination ke splash
                    startDestination = "splash"
                ) {
                    // Rute Splash Screen (BARU)
                    composable("splash") {
                        SplashScreen(navController = navController)
                    }

                    // Rute Home Screen
                    composable("home") {
                        HomeScreen(
                            umkmList = homeVm.umkmList.collectAsState().value,
                            // Tidak ada error lagi jika HomeScreen.kt sudah diubah (lihat Langkah 2)
                            onUmkmClick = { umkmId ->
                                // Logika navigasi ke detail screen
                                navController.navigate("detail/$umkmId")
                            }
                        )
                    }

                    // Rute Detail Screen
                    composable("detail/{umkmId}") { backStackEntry ->
                        val umkmId = backStackEntry.arguments?.getString("umkmId")
                        DetailScreen(
                            umkmId = umkmId,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}