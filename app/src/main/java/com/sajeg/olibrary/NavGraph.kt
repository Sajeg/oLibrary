package com.sajeg.olibrary

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sajeg.olibrary.screens.AccountScreen
import com.sajeg.olibrary.screens.DetailScreen
import com.sajeg.olibrary.screens.HomeScreen
import com.sajeg.olibrary.screens.ScannerScreen
import kotlinx.serialization.Serializable

@Composable
fun SetupNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = HomeScreen(null, null)
    ) {
        composable<HomeScreen> {
            val searchQuery = it.toRoute<HomeScreen>().searchQuery
            val searchFilter = it.toRoute<HomeScreen>().searchFilter
           HomeScreen(navController, searchQuery, searchFilter)
        }
        composable<Details> {
            val id = it.toRoute<Details>().recordId
            val title = it.toRoute<Details>().bookTitle
            DetailScreen(navController = navController, recordId = id, bookTitle = title) }
        composable<QRCode> { ScannerScreen(context = LocalContext.current, navController) }
        composable<Account> { AccountScreen() }
    }
}

@Serializable
data class HomeScreen(
    val searchQuery: String? = null,
    val searchFilter: String? = null
)

@Serializable
object QRCode

@Serializable
object Account

@Serializable
data class Details(
    val recordId: Int,
    val bookTitle: String
)