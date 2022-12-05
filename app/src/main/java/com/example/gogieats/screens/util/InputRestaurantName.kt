package com.example.gogieats.screens.util

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import com.example.gogieats.data.firestore.EditingPost
import com.example.gogieats.screens.addreview.ReviewViewModel
import com.example.gogieats.ui.theme.Orange
import com.example.gogieats.ui.theme.Shadows
import com.example.gogieats.R

@Composable
fun InputRestaurantName2(
    focusRequester: FocusRequester,
    addReviewViewModel: ReviewViewModel? = null,
    post: EditingPost? = null
) {
    (post?.restaurantName?.value ?: addReviewViewModel?.restaurantNameText?.value)?.let {
        TextField(
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .shadow(Shadows().small,
                Shapes().medium,
                spotColor = Color.Gray,
                ambientColor = Color.Transparent),
        label = {
            Text(text = stringResource(R.string.restaurant_name),
                style = MaterialTheme.typography.subtitle1,
                color = Color.Gray)
        },
        value = it,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        onValueChange = { newValue ->
            if (post == null && addReviewViewModel != null) {
                addReviewViewModel.onTextFieldChange(
                    addReviewViewModel.restaurantNameText,
                    newValue
                )
            } else {
                if (post != null) {
                    post.restaurantName.value = newValue
                }
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            textColor = Color.DarkGray,
            disabledTextColor = Color.Transparent,
            backgroundColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedLabelColor = Orange,
            cursorColor = Orange
        ),
        textStyle = LocalTextStyle.current.copy(
            fontSize = MaterialTheme.typography.h6.fontSize,
            textAlign = TextAlign.Center
        )
    )
    }
}