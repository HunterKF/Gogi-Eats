package com.example.kbbqreview.screens.login.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kbbqreview.R
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.util.OrangeButton
import com.example.kbbqreview.ui.theme.Orange



@Composable
fun SignUp(
    modifier: Modifier = Modifier,
) {

    val profilePhoto = Photo() /*= cameraViewModel.getProfilePhoto()*/
    val context = LocalContext.current
    val userName = remember {
        mutableStateOf("")
    }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painter = painterResource(id = R.drawable.logo), contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .bottomElevation()
                )
                Divider(modifier = Modifier
                    .padding(6.dp)
                    .bottomElevation(),
                    thickness = 2.dp,
                    color = Color.Black.copy(0.1f)
                )

                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.h6
                )
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
                            .border(4.dp, Color.Cyan, CircleShape),
                        contentAlignment = Alignment.Center) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth(),
                            model = ImageRequest
                                .Builder(context)
                                .data(R.drawable.profile)
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
                            IconButton(onClick = { /*TODO - viewModel.changeToCreateAccCamera()*/ }) {
                                Icon(
                                    Icons.Rounded.PhotoCamera,
                                    stringResource(id = R.string.take_profile_photo),
                                    tint = Color.Black,
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
                val modifier = Modifier.padding(vertical = 12.dp)
                CustomTextField(icon = Icons.Outlined.Person,
                    text = "Username",
                    label = "e.g.maxpaene",
                    value = userName,
                    modifier = modifier)
                CustomTextField(icon = Icons.Outlined.Email,
                    text = "Email",
                    label = "example@gmail.com",
                    value = userName,
                    modifier = modifier)
                CustomTextField(icon = Icons.Outlined.Password,
                    text = "Password",
                    label = "Enter password",
                    value = userName,
                    modifier = modifier)
                OrangeButton(text = "Next",
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Or continue with "
                    )
                    IconButton(
                        modifier = Modifier
                            .shadow(1.dp,
                                RoundedCornerShape(6.dp),
                                ambientColor = Color.Transparent,
                                spotColor = Color.Gray)
                            .padding(2.dp)

                            .clip(RoundedCornerShape(15.dp))
                            .size(32.dp),
                        onClick = {/*TODO*/ }) {
                        Image(
                            painter = painterResource(id = R.drawable.google_logo),
                            null,
                            modifier = Modifier
                                .offset(y = 1.dp)
                                .size(16.dp)
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
            Text(
                text = "Already registered? Login",
                fontSize = 16.sp
            )
        }
    }

}

@Composable
fun CustomTextField(
    icon: ImageVector,
    text: String = "",
    label: String = "",
    value: MutableState<String>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 7.dp)
            )
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value.value,
            onValueChange = { newValue -> value.value = newValue },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            label = {
                Text(text = label,
                    color = Color.LightGray)
            }
        )
    }
}


private fun Modifier.bottomElevation(): Modifier = this.then(Modifier.drawWithContent {
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
        SignUp()
    }
}