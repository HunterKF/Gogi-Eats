package com.example.kbbqreview.screens.HomeScreen

import com.example.kbbqreview.data.firestore.Post
import com.example.kbbqreview.util.LoginScreenState

sealed class HomeScreenState {
    object Loading : HomeScreenState()
    data class Loaded(
        val posts: List<Post>
    ) : HomeScreenState()

}
