package com.example.gogieats.data.user

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

data class FullUser(
    val blocked_accounts: ArrayList<User>,
    val profile_avatar_local_uri: String,
    val profile_avatar_remote_uri: String,
    val user_id: String,
    val user_name: String,
    val user_name_lowercase: String,
) {
    constructor() : this(
        blocked_accounts = arrayListOf(User()),
        profile_avatar_local_uri = "",
        profile_avatar_remote_uri = "hello",
        user_id = "",
        user_name = "",
        user_name_lowercase = "",
    )
}
