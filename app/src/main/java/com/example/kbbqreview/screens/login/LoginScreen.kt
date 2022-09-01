package com.example.kbbqreview.screens.login

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Facebook
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.kbbqreview.R
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.lang.RuntimeException

@Composable
fun LoginScreen(
    navigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val token = stringResource(R.string.default_web_client_id)
    val gso =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(token)
            .requestEmail().build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    //not best practice?
    var account: GoogleSignInAccount? by remember { mutableStateOf(null) }
    //classic way?
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult() ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        try {
            account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
            Firebase.auth.signInWithCredential(credential).addOnCompleteListener {
                account = GoogleSignIn.getLastSignedInAccount(context)
            }
        } catch (e: ApiException) {
            Log.w("Google", "Google sign in failed", e)
        }
    }

    Box(
        Modifier
            .background(MaterialTheme.colors.surface)
            .fillMaxSize()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.Facebook,
                contentDescription = null,
                modifier = Modifier.size(90.dp),
                tint = MaterialTheme.colors.primary
            )
            Spacer(Modifier.height(20.dp))
            SignInButton(
                onSignedIn = {
                    navigateToHome()
                    Toast.makeText(context, "It signed in!", Toast.LENGTH_SHORT).show()
                },
                onSignInFailed = {
                    Toast.makeText(context, "Try again later.", Toast.LENGTH_SHORT).show()
                }
            )
            Button(onClick = { launcher.launch(googleSignInClient.signInIntent)}) {
                Text("Google")
            }
        }
    }
}

@Composable
fun SignInButton(
    onSignInFailed: (Exception) -> Unit,
    onSignedIn: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    AndroidView(factory = { context ->
        LoginButton(context).apply {
            setPermissions("email", "public_profile")
            val callbackManager = CallbackManager.Factory.create()
            registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    //do nothing
                }

                override fun onError(error: FacebookException) {
                    onSignInFailed(error)
                    println("An error occurred: $error")
                }

                override fun onSuccess(result: LoginResult) {
                    scope.launch {
                        val token = result.accessToken.token
                        val credential = FacebookAuthProvider.getCredential(token)
                        val authResult = Firebase.auth.signInWithCredential(credential).await()
                        if (authResult.user != null) {
                            onSignedIn()
                        } else {
                            println("Could not sign in with Firebase.")
                            onSignInFailed(RuntimeException("Could not sign in with Firebase."))
                        }
                    }

                }

            })
        }
    })
}