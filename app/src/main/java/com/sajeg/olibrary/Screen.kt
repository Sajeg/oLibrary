package com.sajeg.olibrary

sealed class Screen(val route:String) {
    object Home: Screen(route = "home_screen")
    object Details: Screen(route = "detail_screen/{book}")
}