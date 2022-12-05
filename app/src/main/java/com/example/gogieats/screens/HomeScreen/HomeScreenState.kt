package com.example.gogieats.screens.HomeScreen

import com.example.gogieats.data.firestore.Post

sealed class HomeScreenState {
    object Loading : HomeScreenState()
    data class Loaded(
        val posts: List<Post>
    ) : HomeScreenState()

}
