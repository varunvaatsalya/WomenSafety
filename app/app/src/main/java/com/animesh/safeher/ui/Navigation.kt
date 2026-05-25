package com.animesh.safeher.ui

sealed class Screen(val route: String) {
    object Login       : Screen("login")
    object Register    : Screen("register")
    object Home        : Screen("home")        // SOS + live location
    object Map         : Screen("map")         // Hotspot map
    object Contacts    : Screen("contacts")    // Emergency contacts
    object Profile     : Screen("profile")     // User profile
}