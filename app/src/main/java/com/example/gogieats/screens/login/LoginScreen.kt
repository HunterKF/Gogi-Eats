package com.example.gogieats.screens.login

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
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gogieats.ApplicationViewModel
import com.example.gogieats.R
import com.example.gogieats.data.photos.Photo
import com.example.gogieats.screens.camera.CameraViewModel
import com.example.gogieats.screens.camera.ProfileCamera
import com.example.gogieats.screens.camera.ui.theme.Purple500
import com.example.gogieats.screens.login.util.DividedBackground
import com.example.gogieats.screens.login.util.SignIn
import com.example.gogieats.screens.login.util.SignUp
import com.example.gogieats.screens.profile.ProfileViewModel
import com.example.gogieats.screens.util.OrangeButton
import com.example.gogieats.ui.theme.Brown
import com.example.gogieats.ui.theme.Orange
import com.example.gogieats.util.LoginScreenState
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
    navigateToProfile: () -> Unit,
    popBackStack: () -> Unit,
    viewModel: LoginViewModel,
    cameraViewModel: CameraViewModel,
    applicationViewModel: ApplicationViewModel,
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.backToLanding()
        cameraViewModel.profilePicture.clear()
    }
//    viewModel.setCurrentUser(applicationViewModel.currentUser)
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
                    viewModel.changeProfileSettings(navigateToProfile)
                    Toast.makeText(context,
                        context.getString(R.string.signed_in),
                        Toast.LENGTH_SHORT).show()
//                    navigateToProfile()
                }
            } catch (e: ApiException) {
                Log.w("Google", "Google sign in failed", e)
                Toast.makeText(context,
                    context.getString(R.string.failed_sign_in),
                    Toast.LENGTH_SHORT).show()
            }
        }
    val emailFieldState = remember {
        mutableStateOf("")
    }
    val passwordFieldState = remember {
        mutableStateOf("")
    }
    val userName = remember {
        mutableStateOf("")
    }
    val userNameAvailable = remember {
        mutableStateOf(true)
    }

    val userNameChecked = remember {
        mutableStateOf(false)
    }

    when (state) {
        LoginScreenState.SignIn -> {
            Surface(modifier = Modifier.fillMaxSize()) {
                DividedBackground(modifier = Modifier.fillMaxSize())
                SignIn(
                    modifier = Modifier,
                    navigateToHome = navigateToProfile,
                    context = context,
                    launcher = launcher,
                    googleSignInClient = googleSignInClient,
                    popBackStack = popBackStack,
                    emailFieldState = emailFieldState,
                    passwordFieldState = passwordFieldState,
                    viewModel = viewModel
                )
            }
        }
        LoginScreenState.Loading -> {
            Surface(Modifier.fillMaxSize()) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.scale(1.2f))
                }
            }
        }
        LoginScreenState.CreateAccount -> {
            Surface(modifier = Modifier.fillMaxSize()) {
                DividedBackground(modifier = Modifier.fillMaxSize())
                SignUp(
                    viewModel = viewModel,
                    emailFieldState = emailFieldState,
                    passwordFieldState = passwordFieldState,
                    navigateToHome = navigateToProfile,
                    launcher = launcher,
                    googleSignInClient = googleSignInClient,
                    userName = userName,
                    cameraViewModel = cameraViewModel,
                    popBackStack = popBackStack,
                    userNameAvailable = userNameAvailable,
                    userNameChecked = userNameChecked,

                )
            }

        }
        LoginScreenState.LandingScreen -> {
            LandScreenContent(popBackStack, viewModel)
        }
        LoginScreenState.CreateAccCamera -> {
            ProfileCamera(cameraViewModel = cameraViewModel) { viewModel.changeToCreate() }
        }
        LoginScreenState.ChangeSettingCamera -> {
            ProfileCamera(cameraViewModel = cameraViewModel) { viewModel.simpleChangeToProfileSetting() }
        }
        LoginScreenState.ChangeProfileSettings -> {
            AdjustProfileSettings(
                cameraViewModel,
                context,
                viewModel,
                userNameAvailable,
                userNameChecked,
                navigateToProfile
            )
        }
    }
    BackHandler() {
        popBackStack()
    }
}

@Composable
private fun AdjustProfileSettings(
    cameraViewModel: CameraViewModel,
    context: Context,
    viewModel: LoginViewModel,
    userNameAvailable: MutableState<Boolean>,
    userNameChecked: MutableState<Boolean>,
    navigateToProfile: () -> Unit,
) {
    val profileViewModel = ProfileViewModel()
    Box(Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 40.dp)
            .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly) {
            val profilePhoto = cameraViewModel.getProfilePhoto()
            val avatarUrl = profileViewModel.setAvatar()
            val userNameState = remember {
                mutableStateOf("")
            }
            val currentCharCount = remember { mutableStateOf(0) }
            Box(Modifier
                .padding(top = 20.dp, bottom = 10.dp)
                .fillMaxWidth(0.4f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .border(4.dp, Color.Cyan, CircleShape)) {
                AsyncImage(modifier = Modifier.fillMaxSize(),
                    model = ImageRequest.Builder(context)
                        .data(profilePhoto?.localUri ?: avatarUrl)
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
                    IconButton(onClick = { viewModel.changeToSettingsCamera() }) {
                        Icon(
                            Icons.Rounded.PhotoCamera,
                            stringResource(R.string.take_profile_photo),
                            tint = Color.Black,
                            modifier = Modifier.scale(1.3f)
                        )
                    }
                }
            }
            val maxChars = 15
            TextField(
                value = userNameState.value,
                onValueChange = { newValue ->
                    if (newValue.length <= maxChars) {
                        currentCharCount.value = newValue.length
                        viewModel.onTextFieldChange(
                            userNameState,
                            newValue
                        )
                    } else {
                        Toast.makeText(context,
                            context.getString(R.string.shorten_name),
                            Toast.LENGTH_SHORT).show()
                    }
                },
                leadingIcon = { Icon(Icons.Rounded.DriveFileRenameOutline, null) },
                trailingIcon = {
                    IconButton(onClick = {
                        userNameAvailable.value =
                            viewModel.checkUserNameAvailability(userNameState.value,
                                userNameAvailable,
                                context)
                        if (!userNameAvailable.value) {
                            userNameChecked.value = true
                        }
                    }) {
                        Icon(Icons.Rounded.PersonSearch, null)
                    }
                },
                modifier = Modifier
                    .padding()
                    .border(
                        BorderStroke(width = 2.dp,
                            color = if (userNameAvailable.value) Purple500 else Color.Red),
                        shape = RoundedCornerShape(50)
                    )
                    .fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.profile_name),
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
            val currentUser = Firebase.auth.currentUser
            Button(enabled = userNameState.value != "" && profilePhoto != null, onClick = {
                if (profilePhoto != null) {
                    viewModel.createNewAccount(
                        currentUser = currentUser,
                        userName = userNameState.value,
                        context = context,
                        profilePhoto = profilePhoto,
                        navigateToHome = navigateToProfile
                    )
                } else {
                    viewModel.createNewAccount(
                        currentUser = currentUser,
                        userName = userNameState.value,
                        context = context,
                        profilePhoto = Photo(localUri = avatarUrl.localUri),
                        navigateToHome = navigateToProfile)
                }
            }) {
                Text(stringResource(R.string.complete_sign_in))
            }
        }
    }
}

@Composable
private fun LandScreenContent(
    popBackStack: () -> Unit,
    viewModel: LoginViewModel,
) {
    DividedBackground(Modifier.fillMaxSize())
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .shadow(4.dp,
                RoundedCornerShape(15.dp),
                spotColor = Color.LightGray,
                ambientColor = Color.Transparent)
            .clip(RoundedCornerShape(15.dp))
    ) {
        Box {
            IconButton(modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp),
                onClick = {
                    popBackStack()
                }) {
                Icon(Icons.Rounded.Close, null)
            }
            Column(Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Image(modifier = Modifier
                    .scale(0.5f)
                    .weight(1f),
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null)
                Column(Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly) {
                    Text(text = stringResource(R.string.hey_welcome),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.h4,
                    modifier = Modifier.weight(1f))

                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        OrangeButton(
                            text = (stringResource(R.string.get_started)),
                            onClick = { viewModel.changeToCreate() },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(16.dp))

                        TextButton(onClick = { viewModel.changeToSignIn() }) {
                            Row(
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.have_an_account),
                                    fontSize = 20.sp,
                                    style = MaterialTheme.typography.h6,
                                    color = Brown
                                )
                                Text(
                                    text = " Login",
                                    fontSize = 20.sp,
                                    style = MaterialTheme.typography.h6,
                                    color = Orange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}





@Composable
private fun FacebookSignInDefault(
    navigateToHome: () -> Unit,
    context: Context,
    viewModel: LoginViewModel,
) {
    SignInButton(
        onSignedIn = {
            viewModel.changeProfileSettings(navigateToHome)
            Toast.makeText(context, context.getString(R.string.signed_in), Toast.LENGTH_SHORT)
                .show()
        },
        onSignInFailed = {
            Toast.makeText(context, "Try again later. ${it.localizedMessage}", Toast.LENGTH_SHORT)
                .show()
        }
    )
}

//FACEBOOK SIGN IN
@Composable
fun SignInButton(
    onSignInFailed: (Exception) -> Unit,
    onSignedIn: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    Row(Modifier
        .fillMaxWidth()
        .background(Color.Red)) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)) {

        }
        Box(Modifier.background(Color.White)) {
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
                                val authResult =
                                    Firebase.auth.signInWithCredential(credential).await()
                                if (authResult.user != null) {
                                    onSignedIn()
                                } else {
                                    onSignInFailed(RuntimeException(context.getString(R.string.could_not_sign_in)))
                                }
                            }

                        }

                    })
                }
            })
        }
    }
}
