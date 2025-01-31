package com.mushroom.worklog.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Workers : Screen("workers")
    object AddRecord : Screen("add_record")
    object Calculation : Screen("calculation")
    object History : Screen("history")
} 