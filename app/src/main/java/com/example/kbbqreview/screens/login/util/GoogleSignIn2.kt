package com.example.kbbqreview.screens.login.util

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.Image
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
import com.example.kbbqreview.R
import com.google.android.gms.auth.api.signin.GoogleSignInClient

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