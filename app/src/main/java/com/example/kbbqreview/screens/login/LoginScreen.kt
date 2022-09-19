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
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kbbqreview.R
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.camera.CameraViewModel
import com.example.kbbqreview.screens.camera.ProfileCamera
import com.example.kbbqreview.screens.camera.ui.theme.Purple500
import com.example.kbbqreview.util.LoginScreenState
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
    popBackStack: () -> Boolean,
    viewModel: LoginViewModel,
    navigateToCamera: () -> Unit,
    navigateToProfile: () -> Unit,
    navController: NavHostController,
    cameraViewModel: CameraViewModel,
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.backToLanding()
    }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val token = stringResource(com.firebase.ui.auth.R.string.default_web_client_id)
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
    val alreadyHaveAccountState = remember {
        mutableStateOf(false)
    }
    val emailFieldState = remember {
        mutableStateOf("")
    }
    val passwordFieldState = remember {
        mutableStateOf("")
    }
    val reEnterPasswordState = remember {
        mutableStateOf("")
    }
    val userName = remember {
        mutableStateOf("")
    }
    val userNameAvailable = remember {
        mutableStateOf(true)
    }



    when (state) {
        LoginScreenState.SignIn -> {
            println("Current state is ${state}")
            SignInScreen(
                navigateToHome,
                context,
                launcher,
                googleSignInClient,
                popBackStack,
                emailFieldState,
                passwordFieldState,
                viewModel
            )
        }
        LoginScreenState.Loading -> {
            println("Current state is ${state}")
            CircularProgressIndicator()
        }
        LoginScreenState.CreateAccount -> {
            CreateAccountScreen(
                viewModel,
                context,
                emailFieldState,
                passwordFieldState,
                reEnterPasswordState,
                navigateToHome,
                launcher,
                googleSignInClient,
                userName,
                cameraViewModel,
                popBackStack,
                userNameAvailable
            )

            println("Current state is ${state}")


        }
        LoginScreenState.LandingScreen -> {
//            Image by <a href="https://www.freepik.com/free-vector/hand-drawn-korean-bbq-illustration_31216894.htm#query=illustrations%20korean%20bbq&position=0&from_view=search">Freepik</a>
            Box {
                IconButton(modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp),
                    onClick = {
                        navigateToHome()
                    }) {
                    Icon(Icons.Rounded.ArrowBack, null)
                }
                Column(Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(modifier = Modifier
                        .scale(0.7f)
                        .weight(1f),
                        painter = painterResource(id = R.drawable.landing_page_image),
                        contentDescription = null)
                    Column(Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Hey! Welcome", style = MaterialTheme.typography.h6)

                        Button(onClick = { viewModel.changeToCreate() }) {
                            Text("Get started")
                        }
                        TextButton(onClick = { viewModel.changeToSignIn() }) {
                            Text("Already have an account?")
                        }
                    }

                }
            }
        }
        LoginScreenState.Camera -> {
            ProfileCamera(cameraViewModel = cameraViewModel, viewModel = viewModel)
        }
    }


    BackHandler() {
        popBackStack()
    }
}

@Composable
private fun SignInScreen(
    navigateToHome: () -> Unit,
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    googleSignInClient: GoogleSignInClient,
    popBackStack: () -> Boolean,
    emailFieldState: MutableState<String>,
    passwordFieldState: MutableState<String>,
    viewModel: LoginViewModel,
) {
    Box(
        Modifier
            .background(MaterialTheme.colors.surface)
            .fillMaxSize()
    ) {
        Box(Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = { popBackStack() }) {
                Icon(Icons.Rounded.Cancel, null, tint = Color.DarkGray)
            }
        }

        Column(
            Modifier.padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(90.dp))
            EmailSignIn(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth(),
                emailFieldState = emailFieldState,
                passwordFieldState = passwordFieldState,
                viewModel = viewModel,
            )
            Spacer(Modifier.weight(0.1f))
            Divider()
            Spacer(Modifier.weight(0.1f))
            FacebookSignInDefault(navigateToHome, context)
            GoogleSignIn(launcher, googleSignInClient)
            Spacer(modifier = Modifier.weight(0.1f))
        }

    }
}

@Composable
private fun CreateAccountScreen(
    viewModel: LoginViewModel,
    context: Context,
    emailFieldState: MutableState<String>,
    passwordFieldState: MutableState<String>,
    reEnterPasswordState: MutableState<String>,
    navigateToHome: () -> Unit,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    googleSignInClient: GoogleSignInClient,
    userName: MutableState<String>,
    cameraViewModel: CameraViewModel,
    popBackStack: () -> Boolean,
    userNameAvailable: MutableState<Boolean>
) {
    val scrollState = rememberScrollState()
    val profilePhoto = cameraViewModel.getLastPhoto()
    val currentCharCount = remember { mutableStateOf(0) }
    Box(Modifier.fillMaxSize()) {
        Box(Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = { popBackStack() }) {
                Icon(Icons.Rounded.Cancel, null, tint = Color.DarkGray)
            }
        }

        Column(
            Modifier
                .padding(horizontal = 30.dp)
                .scrollable(scrollState, orientation = Orientation.Vertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            ) {

            Box(Modifier
                .padding(top = 20.dp, bottom = 10.dp)
                .fillMaxWidth(0.4f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .border(4.dp, Color.Cyan, CircleShape)) {
                AsyncImage(modifier = Modifier.fillMaxSize(),
                    model = ImageRequest
                        .Builder(context)
                        .data(profilePhoto?.localUri ?: R.drawable.profile)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
                Row(Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White.copy(0.4f)),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.changeToCamera() }) {
                        Icon(
                            Icons.Rounded.PhotoCamera, "Take profile picture", tint = Color.Black,
                            modifier = Modifier.scale(1.3f)
                        )
                    }
                }
            }


            CreateAccountInfo(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                padding = 8.dp,
                emailFieldState = emailFieldState,
                passwordFieldState = passwordFieldState,
                viewModel = viewModel,
                userName = userName,
                userNameAvailable = userNameAvailable,
                currentCharCount = currentCharCount,
                profilePhoto = profilePhoto
            )
            Spacer(Modifier.weight(0.1f))
            Divider()
            Spacer(Modifier.weight(0.1f))
            FacebookSignInDefault(navigateToHome, context)
            GoogleSignIn(launcher, googleSignInClient)
            Spacer(modifier = Modifier.weight(0.1f))
        }
    }
}


@Composable
private fun GoogleSignIn(
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    googleSignInClient: GoogleSignInClient,
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth(),
        onClick = { launcher.launch(googleSignInClient.signInIntent) }) {
        Icon(Icons.Rounded.Email, contentDescription = null)
        Text("Continue with Google")
    }
}

@Composable
private fun FacebookSignInDefault(
    navigateToHome: () -> Unit,
    context: Context,
) {
    SignInButton(
        onSignedIn = {
            navigateToHome()
            Toast.makeText(context, "It signed in!", Toast.LENGTH_SHORT).show()
        },
        onSignInFailed = {
            Toast.makeText(context, "Try again later.", Toast.LENGTH_SHORT).show()
        }
    )
}

@Composable
fun CreateAccountInfo(
    modifier: Modifier,
    padding: Dp,
    emailFieldState: MutableState<String>,
    passwordFieldState: MutableState<String>,
    viewModel: LoginViewModel,
    userName: MutableState<String>,
    userNameAvailable: MutableState<Boolean>,
    currentCharCount: MutableState<Int>,
    profilePhoto: Photo?,
) {
    val context = LocalContext.current
    val auth = Firebase.auth
    val createAccountLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val task =
                auth.createUserWithEmailAndPassword(emailFieldState.value, passwordFieldState.value)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Login", "createUserWithEmail: success")
                            val user = auth.currentUser
                        } else {
                            Log.w("Login", "createUserWithEmail: failure", task.exception)
                            Toast.makeText(
                                context, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
        }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier,
    ) {
        TextField(
            value = emailFieldState.value,
            onValueChange = { newValue -> emailFieldState.value = newValue },
            leadingIcon = { Icon(Icons.Rounded.Email, null) },
            modifier = Modifier
                .padding(vertical = padding)
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            maxLines = 1,
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),

            )
        val maxChars = 15
        TextField(
            value = userName.value,
            onValueChange = { newValue ->
                if (newValue.length <= maxChars) {
                    currentCharCount.value = newValue.length
                    viewModel.onTextFieldChange(
                        userName,
                        newValue
                    )
                } else {
                    Toast.makeText(context, "Shorten name.", Toast.LENGTH_SHORT).show()
                }},
            leadingIcon = { Icon(Icons.Rounded.DriveFileRenameOutline, null) },
            trailingIcon = {
                IconButton(onClick = { userNameAvailable.value = viewModel.checkUserNameAvailability(userName.value, userNameAvailable) }) {
                    Icon(Icons.Rounded.PersonSearch, null)
                }
            },
            modifier = Modifier
                .padding(vertical = padding)
                .border(
                    BorderStroke(width = 2.dp, color = if (userNameAvailable.value) Purple500 else Color.Red),
                    shape = RoundedCornerShape(50)
                )
                .fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Profile name",
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
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = "Password visibility")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .padding(vertical = padding)
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
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            maxLines = 1,
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Button(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = padding),
            enabled = emailFieldState.value.isNotEmpty() && passwordFieldState.value.isNotEmpty() && userName.value.isNotEmpty(),
            onClick = {
                viewModel.createAccount(emailFieldState.value, passwordFieldState.value, userName.value, profilePhoto)
            }) {
            Text(text = "Create Account")
        }
        TextButton(onClick = {
            viewModel.changeToSignIn()
        }) {
            Text("Already have an account?")
        }


    }
}

@Composable
fun EmailSignIn(
    modifier: Modifier,
    emailFieldState: MutableState<String>,
    passwordFieldState: MutableState<String>,
    viewModel: LoginViewModel,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ) {
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
        Button(modifier = Modifier.fillMaxWidth(),
            enabled = emailFieldState.value.isNotEmpty() && passwordFieldState.value.isNotEmpty(),
            onClick = {
                println("Attempting to sign in")
                viewModel.signInWithEmailAndPassword(
                    emailFieldState.value,
                    passwordFieldState.value
                )
            }) {
            Text(text = "Sign In")
        }

        val scope = rememberCoroutineScope()
        TextButton(onClick = {
            viewModel.changeToCreate()
        }) {
            Text("New? Create account")
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