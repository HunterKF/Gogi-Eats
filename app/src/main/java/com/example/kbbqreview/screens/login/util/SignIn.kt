package com.example.kbbqreview.screens.login.util

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.kbbqreview.R
import com.example.kbbqreview.screens.login.LoginViewModel
import com.example.kbbqreview.screens.util.OrangeButton
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun SignIn(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit,
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    googleSignInClient: GoogleSignInClient,
    popBackStack: () -> Boolean,
    emailFieldState: MutableState<String>,
    passwordFieldState: MutableState<String>,
    viewModel: LoginViewModel,
) {
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
                modifier = Modifier.fillMaxWidth().zIndex(1f),
                ) {
                IconButton(
                    modifier = Modifier.align(Alignment.TopStart),
                    onClick = { viewModel.backToLanding() }) {
                    Icon(Icons.Rounded.ArrowBack, null, tint = Color.DarkGray)
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
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
                        CustomTextField(icon = Icons.Outlined.Email,
                            text = "Email",
                            label = "example@gmail.com",
                            value = emailFieldState,
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
                        OrangeButton(text = stringResource(id = R.string.sign_in),
                            onClick = {
                                viewModel.signInWithEmailAndPassword(
                                    context,
                                    emailFieldState.value,
                                    passwordFieldState.value,
                                    navigateToHome
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
    }
}