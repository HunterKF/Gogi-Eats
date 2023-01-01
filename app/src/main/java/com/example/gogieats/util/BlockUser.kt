package com.example.gogieats.util

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.gogieats.data.user.FullUser
import com.example.gogieats.data.user.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BlockUser {
    companion object {
        val db = Firebase.firestore
        fun blockUser(currentUserUid: String, blockedUser: User) {
            db.collection("users").document(currentUserUid).update("blocked_accounts", FieldValue.arrayUnion(
                blockedUser))
                .addOnSuccessListener {
                    Log.d("BlockedUser", "Successfully blocked the user")
                }
                .addOnFailureListener {
                    Log.d("BlockedUser", "Failed to block the user. ${it.localizedMessage}")
                }
        }
        fun unblockUser(currentUserUid: String, blockedUser: User) {
            db.collection("users").document(currentUserUid).update("blocked_accounts", FieldValue.arrayRemove(blockedUser))
                .addOnSuccessListener {
                    Log.d("BlockedUser", "Successfully blocked the user")
                }
                .addOnFailureListener {
                    Log.d("BlockedUser", "Failed to block the user. ${it.localizedMessage}")
                }
        }
        fun getBlockedAccounts(uid: String): ArrayList<User> {
            val db = Firebase.firestore.collection("users")
           var blockedAccounts = mutableStateOf(arrayListOf<User>())
            db.document(uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val fullUser = documentSnapshot.toObject(FullUser::class.java)
                    fullUser?.let { account ->
                        account.blocked_accounts.forEach {
                            Log.d("BlockUser", "Adding a blocked account.")

                            blockedAccounts.value.add(it)
                            Log.d("BlockUser", "Current blocked account size: ${blockedAccounts.value.size}")

                        }
                        return@addOnSuccessListener

                    }
                }
                .addOnFailureListener {
                    Log.d("BlockUser", "Failed to get blocked accounts. ${it.localizedMessage}")
                }
            Log.d("BlockUser", "Returning function: ${blockedAccounts.value.size}")

            return blockedAccounts.value
        }
    }
}