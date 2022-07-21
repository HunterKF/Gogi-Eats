package com.example.kbbqreview

sealed class Screen(val route: String) {
    object MapScreen : Screen("map_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
