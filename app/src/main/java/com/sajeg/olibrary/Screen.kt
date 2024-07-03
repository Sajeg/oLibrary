package com.sajeg.olibrary

sealed class Screen(val route: String) {
    data object Home : Screen(route = "home_screen")
    data object Details : Screen(route = "detail_screen")
    data object QRCode : Screen(route = "qrcode_screen")
    data object Account : Screen(route = "account_screen")
}