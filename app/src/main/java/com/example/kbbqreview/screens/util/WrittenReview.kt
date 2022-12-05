package com.example.kbbqreview.screens.util

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kbbqreview.R
import com.example.kbbqreview.screens.addreview.ReviewViewModel
import com.example.kbbqreview.ui.theme.Brown
import com.example.kbbqreview.ui.theme.Orange
import com.example.kbbqreview.ui.theme.Shadows

@Composable
fun WrittenReview(
    reviewViewModel: ReviewViewModel? = null,
    modifier: Modifier = Modifier,
    currentCharCount: MutableState<Int>,
    authorText: MutableState<String>? = null,
) {
    val context = LocalContext.current
    val maxChars = 1000



    (reviewViewModel?.restaurantReviewText?.value ?: authorText?.value)?.let {
        OutlinedTextField(
            modifier = modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 120.dp)
                .shadow(
                    Shadows().extraSmall,
                    RoundedCornerShape(10.dp),
                    spotColor = Color.Gray,
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
                        text = "${currentCharCount.value} / 1000"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            value = it,
            onValueChange = { newValue ->
                if (newValue.length <= maxChars) {
                    currentCharCount.value = newValue.length
                    reviewViewModel?.onTextFieldChange(
                        reviewViewModel.restaurantReviewText,
                        newValue
                    )
                    authorText?.value = newValue
                } else {
                    Toast.makeText(context,
                        context.getString(R.string.short_review),
                        Toast.LENGTH_SHORT).show()
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                textColor = Color.Gray,
                disabledTextColor = Color.Transparent,
                backgroundColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedLabelColor = Orange,
                cursorColor = Brown

            ),
            textStyle = LocalTextStyle.current.copy(
                fontSize = MaterialTheme.typography.body1.fontSize,
                textAlign = TextAlign.Left
            )

        )
    }

}

