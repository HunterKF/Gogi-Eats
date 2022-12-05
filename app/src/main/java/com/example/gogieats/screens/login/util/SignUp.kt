package com.example.gogieats.screens.login.util

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gogieats.R
import com.example.gogieats.screens.camera.CameraViewModel
import com.example.gogieats.screens.login.LoginViewModel
import com.example.gogieats.screens.util.OrangeButton
import com.example.gogieats.ui.theme.Yellow
import com.google.android.gms.auth.api.signin.GoogleSignInClient


@Composable
fun SignUp(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel,
    emailFieldState: MutableState<String>,
    passwordFieldState: MutableState<String>,
    navigateToHome: () -> Unit,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    googleSignInClient: GoogleSignInClient,
    userName: MutableState<String>,
    cameraViewModel: CameraViewModel,
    popBackStack: () -> Unit,
    userNameAvailable: MutableState<Boolean>,
    userNameChecked: MutableState<Boolean>,
) {

    val profilePhoto = cameraViewModel.getProfilePhoto()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .scrollable(scrollState, orientation = Orientation.Vertical)
    ) {
        Card(
            modifier = modifier
                .padding(12.dp)
                .shadow(4.dp,
                    RoundedCornerShape(15.dp),
                    spotColor = Color.LightGray,
                    ambientColor = Color.Transparent)
                .clip(RoundedCornerShape(15.dp))
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(
                    modifier = Modifier.padding(4.dp).align(Alignment.TopStart),
                    onClick = { viewModel.backToLanding() }) {
                    Icon(Icons.Rounded.ArrowBack, null, tint = Color.DarkGray)
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(1f),
                        ) {

                            Image(painter = painterResource(id = R.drawable.logo),
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(90.dp)
                                    .bottomElevation()
                            )
                        }
                    }
                    item {
                        Divider(modifier = Modifier
                            .padding(6.dp)
                            .bottomElevation(),
                            thickness = 2.dp,
                            color = Color.Black.copy(0.1f)
                        )
                    }

                    item {
                        Text(
                            text = "Sign Up",
                            style = MaterialTheme.typography.h6
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(100.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                Modifier
                                    .size(70.dp)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .border(2.dp, Yellow, CircleShape)
                                    .clickable {
                                        viewModel.changeToCreateAccCamera()
                                    },
                                contentAlignment = Alignment.Center) {
                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    model = ImageRequest
                                        .Builder(context)
                                        .data(profilePhoto?.localUri ?: R.drawable.profile)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                                if (profilePhoto == null) {
                                    Row(Modifier
                                        .align(Alignment.Center)
                                        .fillMaxSize()
                                        .background(Color.White.copy(0.4f)),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.PhotoCamera,
                                            stringResource(id = R.string.take_profile_photo),
                                            tint = Yellow,
                                            modifier = Modifier.scale(1.3f)
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "Upload profile picture"
                                )
                            }
                        }
                    }
                    val modifier = Modifier.padding(vertical = 12.dp)
                    val maxChars = 15
                    item {
                        val currentCharCount = remember { mutableStateOf(0) }

                        CustomTextField(icon = Icons.Outlined.Person,
                            text = "Username",
                            label = "e.g.maxpaene",
                            value = emailFieldState,
                            modifier = modifier,
                            maxChars = maxChars,
                            currentCharCount = currentCharCount,
                            context = context,
                            focusManager = focusManager,
                            isUserName = true,
                            viewModel = viewModel,
                            userNameAvailable = userNameAvailable,
                            userNameChecked = userNameChecked,
                            keyboardType = KeyboardType.Password
                        )
                    }

                    item {
                        CustomTextField(icon = Icons.Outlined.Email,
                            text = "Email",
                            label = "example@gmail.com",
                            value = userName,
                            modifier = modifier,
                            context = context,
                            focusManager = focusManager,
                            viewModel = viewModel,
                            keyboardType = KeyboardType.Text

                        )
                    }
                    item {
                        CustomTextField(icon = Icons.Outlined.Password,
                            text = "Password",
                            label = "Enter password",
                            value = passwordFieldState,
                            modifier = modifier,
                            maxChars = null,
                            currentCharCount = null,
                            context = context,
                            isPassword = true,
                            focusManager = focusManager,
                            viewModel = viewModel,
                            keyboardType = KeyboardType.Password
                        )
                    }
                    item {
                        OrangeButton(text = "Next",
                            onClick = {
                                viewModel.createAccount(
                                    emailFieldState.value,
                                    passwordFieldState.value,
                                    userName.value,
                                    profilePhoto,
                                    navigateToHome,
                                    context
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                        )
                        GoogleSignIn2(
                            launcher, googleSignInClient
                        )
                    }

                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { viewModel.changeToSignIn() }) {
                Text(
                    text = "Already registered? Login",
                    fontSize = 16.sp
                )
            }

        }
    }

}


fun Modifier.bottomElevation(): Modifier = this.then(Modifier.drawWithContent {
    val paddingPx = 8.dp.toPx()
    clipRect(
        left = 0f,
        top = 0f,
        right = size.width,
        bottom = size.height + paddingPx
    ) {
        this@drawWithContent.drawContent()
    }
})

@Preview(showSystemUi = true)
@Composable
fun SignUpPreview() {
    Surface(modifier = Modifier.fillMaxSize()) {
        DividedBackground(modifier = Modifier.fillMaxSize())
//        SignUp()
    }
}