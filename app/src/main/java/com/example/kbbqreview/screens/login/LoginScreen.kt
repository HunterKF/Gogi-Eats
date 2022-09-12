package com.example.kbbqreview.screens.login

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Facebook
import androidx.compose.material.icons.rounded.Password
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.kbbqreview.R
import com.example.kbbqreview.screens.camera.ui.theme.Purple500
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
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
    navigateToHome: () -> Unit,
    popBackStack: () -> Boolean
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
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
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
        IconButton(modifier = Modifier.align(Alignment.TopStart), onClick = { popBackStack() }) {
            Icon(Icons.Rounded.Cancel, null)
        }
        Column(
            Modifier.padding(horizontal = 30.dp)
        ) {
            CreateAccount(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth()
            )
            Spacer(Modifier.weight(0.1f))
            Divider()
            Spacer(Modifier.weight(0.1f))
            LoginButtons(
                modifier = Modifier.weight(1f),
                navigateToHome = navigateToHome,
                context = context,
                launcher = launcher,
                googleSignInClient = googleSignInClient
            )
        }

    }
    BackHandler() {
        popBackStack()
    }
}

@Composable
private fun LoginButtons(
    navigateToHome: () -> Unit,
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    googleSignInClient: GoogleSignInClient,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        Button(
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(),
            onClick = { launcher.launch(googleSignInClient.signInIntent) }) {
            Icon(Icons.Rounded.Facebook, contentDescription = null)
            Text("Continue with Google")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { launcher.launch(googleSignInClient.signInIntent) }) {
            Text("Continue with Facebook")
        }
    }
}

@Composable
fun CreateAccount(modifier: Modifier) {
    val emailFieldState = remember {
        mutableStateOf("")
    }
    val passwordFieldState = remember {
        mutableStateOf("")
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ) {
        Text(
            text = "Create an Account",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        TextField(
            value = emailFieldState.value,
            onValueChange = { newValue -> emailFieldState.value = newValue },
            leadingIcon = { Icon(Icons.Rounded.Email, null) },
            modifier = Modifier
                .border(
                    BorderStroke(width = 2.dp, color = Purple500),
                    shape = RoundedCornerShape(50)
                )
                .fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Email address",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.subtitle1
                )
            },
            maxLines = 1,
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        TextField(
            value = passwordFieldState.value,
            onValueChange = { newValue -> passwordFieldState.value = newValue },
            leadingIcon = { Icon(Icons.Rounded.Password, null) },
            modifier = Modifier
                .border(
                    BorderStroke(width = 2.dp, color = Purple500),
                    shape = RoundedCornerShape(50)
                )
                .fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Password",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.subtitle1
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            maxLines = 1,
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = { /*TODO*/ }) {
            Text(text = "Create Account")
        }
        TextButton(onClick = { /*TODO*/ }) {
            Text("Already have an account?")
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