package com.example.gogieats.screens.login.util

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.example.gogieats.R
import com.example.gogieats.ui.theme.Shadows


@Composable
fun GoogleSignIn2(
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    googleSignInClient: GoogleSignInClient,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    launcher.launch(googleSignInClient.signInIntent)
                }
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Or continue with "
        )
        Box(
            modifier = Modifier

                .padding(5.dp)
                .size(30.dp)
                .shadow(Shadows().small,
                    androidx.compose.material.Shapes().medium,
                    spotColor = Color.Gray,
                    ambientColor = Color.Transparent)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White),
        contentAlignment = Alignment.Center) {
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