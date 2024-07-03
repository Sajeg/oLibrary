package com.sajeg.olibrary

sealed class Screen(val route: String) {
    data object Home : Screen(route = "home_screen")
    data object Details : Screen(route = "detail_screen")
    data object QRCode : Screen(route = "qrcode_screen")
    data object Account : Screen(route = "account_screen")

    fun withStringArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach {
                append("/$it")
            }
        }
    }

    fun withIntArgs(vararg args: Int): String {
        return buildString {
            append(route)
            args.forEach {
                append("/$it")
            }
        }
    }
}