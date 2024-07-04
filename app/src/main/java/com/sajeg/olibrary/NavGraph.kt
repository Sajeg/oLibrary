package com.sajeg.olibrary

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sajeg.olibrary.details.DetailScreen
import com.sajeg.olibrary.qrcodescanner.ScannerScreen
import com.sajeg.olibrary.webview.AccountScreen
import kotlinx.serialization.Serializable

@Composable
fun SetupNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = HomeScreen
    ) {
        composable<HomeScreen> { HomeScreen(navController) }
        composable<Details> {
            val id = it.toRoute<Details>().recordId
            val title = it.toRoute<Details>().bookTitle
            DetailScreen(navController = navController, recordId = id, bookTitle = title) }
        composable<QRCode> { ScannerScreen(context = LocalContext.current) }
        composable<Account> { AccountScreen() }
    }
}

@Serializable
object HomeScreen

@Serializable
object QRCode

@Serializable
object Account

@Serializable
data class Details(
    val recordId: Int,
    val bookTitle: String
)