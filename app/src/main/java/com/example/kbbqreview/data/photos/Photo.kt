package com.example.kbbqreview.data.photos

data class Photo(
    val localUri: String = "",
    var remoteUri: String = "",
    var firebaseId: String = "",
    var listIndex: Int = 0
) {

}
