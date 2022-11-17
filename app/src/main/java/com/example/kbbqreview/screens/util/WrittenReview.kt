package com.example.kbbqreview.screens.util

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.kbbqreview.R
import com.example.kbbqreview.screens.addreview.ReviewViewModel
import com.example.kbbqreview.ui.theme.Shadows

@Composable
fun WrittenReview(
    reviewViewModel: ReviewViewModel,
    modifier: Modifier = Modifier,
    currentCharCount: MutableState<Int>,
) {
    val context = LocalContext.current
    val maxChars = 1000

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3.5f)
            .shadow(Shadows().small,
                Shapes().medium,
                spotColor = Color.DarkGray,
                ambientColor = Color.Transparent)
            .background(Color.White),
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.optional_write_about_it))
                Text(
                    modifier = Modifier
                        .offset(x = (-2).dp, y = (-4).dp),
                    text = "${currentCharCount.value} / 1000"
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        value = reviewViewModel.restaurantReviewText.value,
        onValueChange = { newValue ->
            if (newValue.length <= maxChars) {
                currentCharCount.value = newValue.length
                reviewViewModel.onTextFieldChange(
                    reviewViewModel.restaurantReviewText,
                    newValue
                )
            } else {
                Toast.makeText(context,
                    context.getString(R.string.short_review),
                    Toast.LENGTH_SHORT).show()
            }
        }

    )

}

