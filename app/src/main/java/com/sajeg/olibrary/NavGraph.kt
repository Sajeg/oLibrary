package com.sajeg.olibrary

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sajeg.olibrary.details.DisplayBookInfo
import com.sajeg.olibrary.qrcodescanner.ScannerScreen
import com.sajeg.olibrary.webview.AccountScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(
            route = Screen.Home.route
        ) {
            HomeScreen(navController)
        }
        composable(
            route = Screen.Details.route,
            arguments = listOf(navArgument("book") {
                type = NavType.IntType
                nullable = false
            })
        ) { entry ->
            DisplayBookInfo(entry.arguments.getInt("book"))
        }
        composable(
            route = Screen.QRCode.route
        ) {
            ScannerScreen(context = LocalContext.current)
        }
        composable(
            route = Screen.Account.route
        ) {
            AccountScreen()
        }
    }
}