package com.example.gogieats.data.photos

data class Photo(
    var localUri: String = "",
    var remoteUri: String = "",
    var firebaseId: String = "",
    var listIndex: Int = 0
) {

}