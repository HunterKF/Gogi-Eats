package com.example.gogieats.screens.login.util

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gogieats.R
import com.example.gogieats.screens.login.LoginViewModel
import com.example.gogieats.screens.profile.ProfileViewModel
import com.example.gogieats.ui.theme.Brown
import com.example.gogieats.ui.theme.Orange
import com.example.gogieats.ui.theme.Yellow

@Composable
fun CustomTextField(
    icon: ImageVector,
    text: String = "",
    label: String = "",
    value: MutableState<String>,
    modifier: Modifier = Modifier,
    maxChars: Int? = null,
    currentCharCount: MutableState<Int>? = null,
    context: Context,
    isPassword: Boolean = false,
    focusManager: FocusManager,
    isUserName: Boolean = false,
    viewModel: LoginViewModel? = null,
    userNameAvailable: MutableState<Boolean>? = null,
    userNameChecked: MutableState<Boolean>? = null,
    keyboardType: KeyboardType,
    profileViewModel: ProfileViewModel? = null
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

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
            onValueChange = { newValue ->
                if (maxChars == null) value.value = newValue else {
                    if (newValue.length <= maxChars) {
                        currentCharCount!!.value = newValue.length
                        value.value = newValue
                    } else {
                        Toast.makeText(context,
                            context.getString(R.string.shorten_name),
                            Toast.LENGTH_SHORT).show()
                    }
                }
            },
            singleLine = true,
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = null,
                        tint = Brown)
                    }
                } else if (isUserName) {
                    IconButton(onClick = {
                        if (viewModel != null) {
                            userNameAvailable!!.value =
                                viewModel.checkUserNameAvailability(value.value,
                                    userNameAvailable,
                                    context)
                        } else if (profileViewModel != null) {
                            userNameAvailable!!.value =
                                profileViewModel.checkUserNameAvailability(value.value,
                                    userNameAvailable,
                                    context)
                        }
                        if (userNameAvailable != null) {
                            if (!userNameAvailable.value) {
                                userNameChecked!!.value = true
                            }
                        }
                    }) {
                        Icon(Icons.Rounded.PersonSearch, null,
                            tint = Brown)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            keyboardActions = KeyboardActions {
                if (!isPassword) {
                    focusManager.moveFocus(FocusDirection.Down)
                } else {
                    focusManager.clearFocus()
                }
            },
            maxLines = 1,
            visualTransformation = if (!isPassword || passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(8.dp),
            label = {
                Text(
                    text = label,
                    color = Color.LightGray
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                cursorColor = Orange,
                focusedBorderColor = Yellow,
                unfocusedBorderColor = Brown,
                focusedLabelColor = Yellow
            )
        )
    }
}