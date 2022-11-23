package com.example.kbbqreview.data.user

data class FullUser(
    val profile_avatar_local_uri: String,
    val profile_avatar_remote_uri: String,
    val user_id: String,
    val user_name: String,
    val user_name_lowercase: String
) {
    constructor() : this("", "hello","", "","")
}
