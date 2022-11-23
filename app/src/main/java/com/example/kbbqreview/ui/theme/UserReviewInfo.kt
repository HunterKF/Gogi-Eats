package com.example.kbbqreview.ui.theme

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.example.kbbqreview.R
import com.example.kbbqreview.data.firestore.Post
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.data.user.FullUser
import com.example.kbbqreview.screens.util.ProfileImage
import com.example.kbbqreview.util.AddressMap
import com.example.kbbqreview.util.HandleUser
import com.example.kbbqreview.util.ShareUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UserReviewInfo(
    modifier: Modifier = Modifier,
    post: Post,
) {

    var userOfPost = remember {
        mutableStateOf("")
    }
    LaunchedEffect(key1 = Unit, block = {
        userOfPost.value = HandleUser.getUser(post.userId)
    })


    val constraints = ConstraintSet {
        val greenBox = createRefFor("greenbox")
        val redBox = createRefFor("redbox")

        val userInfo = createRefFor("userinfo")
        val addressIcon = createRefFor("addressicon")
        val reviewComment = createRefFor("reviewcomment")
        val addressBar = createRefFor("addressbar")

        constrain(userInfo) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
        }
        constrain(addressIcon) {
            top.linkTo(reviewComment.bottom)
            start.linkTo(parent.start)
            bottom.linkTo(parent.bottom)
//            end.linkTo(addressBar.start)
        }
        constrain(reviewComment) {
            top.linkTo(parent.top)
            start.linkTo(userInfo.end)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
        }
        constrain(addressBar) {
            top.linkTo(addressIcon.top)
            start.linkTo(reviewComment.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
        }
        createHorizontalChain(userInfo, reviewComment)
        createHorizontalChain(addressIcon, addressBar, chainStyle = ChainStyle.Packed)
        createVerticalChain(userInfo, addressIcon, chainStyle = ChainStyle.SpreadInside)
        createVerticalChain(reviewComment, addressBar, chainStyle = ChainStyle.SpreadInside)

    }
    ConstraintLayout(constraints, modifier = modifier) {
        Box(modifier = Modifier
            .layoutId("userinfo")
        ) {
            Column() {
                ProfileImage(avatarUrl = userOfPost.value)
                Text(
                    text = post.authorDisplayName,
                    style = MaterialTheme.typography.h6,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brown
                )
            }

        }
        Box(modifier = Modifier
            .size(40.dp)
            .layoutId("addressicon"),
        contentAlignment = Alignment.Center) {
            Icon(painter = painterResource(id = R.drawable.icon_address),
                contentDescription = null,
                tint = Brown,
            modifier = Modifier.size(24.dp))
        }
        Box(
            modifier = Modifier
                .layoutId("reviewcomment")
                .padding(start = 6.dp)
        ) {
            ReviewComment2(post = post, modifier = Modifier)
        }
        Box(
            modifier = Modifier
                .layoutId("addressbar")
                .padding(start = 6.dp)
        ) {
            ReviewAddress(post = post)

        }
    }
    /*Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.weight(1f))

            }
            Column(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Spacer(Modifier.height(6.dp))

            }

        }

    }*/
}

@Composable
fun ReviewComment2(post: Post, modifier: Modifier = Modifier) {
    val MINIMIZED_MAX_LINES = 3
    var isExpanded by remember { mutableStateOf(false) }
    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }
    var isClickable by remember { mutableStateOf(false) }
    var finalText by remember { mutableStateOf(post.authorText) }
    val textLayoutResult = textLayoutResultState.value

    LaunchedEffect(textLayoutResult) {
        if (textLayoutResult == null) return@LaunchedEffect

        when {
            isExpanded -> {
                finalText = "${post.authorText} Show Less"

            }
            !isExpanded && textLayoutResult.hasVisualOverflow -> {
                val lastCharIndex = textLayoutResult.getLineEnd(MINIMIZED_MAX_LINES - 1)
                val showMoreString = "... Show More"
                val adjustedText = post.authorText
                    .substring(startIndex = 0,
                        endIndex = lastCharIndex)
                    .dropLast(showMoreString.length)
                    .dropLastWhile { it == ' ' || it == '.' }

                finalText = "$adjustedText$showMoreString"

                isClickable = true
            }
        }
    }
    Text(
        text = buildAnnotatedString {
            append(finalText)
        },
        maxLines = if (isExpanded) Int.MAX_VALUE else MINIMIZED_MAX_LINES,
        onTextLayout = { textLayoutResultState.value = it },
        style = MaterialTheme.typography.subtitle1,
        color = Color.Gray,
        fontSize = 15.sp,
        lineHeight = 18.sp,
        modifier = Modifier
            .clickable(enabled = isClickable) { isExpanded = !isExpanded }
            .animateContentSize(),
    )
}

@Composable
fun ReviewAddress(post: Post, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val emptyPhoto = Photo(
        "",
        "",
        "",
        0
    )
    val address = AddressMap.getAddressFromLocation(context,
        post.location!!.latitude,
        post.location.longitude)
    val shareIntent = ShareUtils.genericShare(
        context = context,
        text = post.restaurantName,
        address = address,
        photoUri = if (post.photoList.isEmpty()) emptyPhoto.remoteUri else post.photoList.first().remoteUri
    )
    val mapIntent: Intent = Uri.parse(
        "geo:${post.location.latitude},${post.location.longitude}?z=8"
    ).let { location ->
        // Or map point based on latitude/longitude
        // val location: Uri = Uri.parse("geo:37.422219,-122.08364?z=14") // z param is zoom level
        Intent(Intent.ACTION_VIEW, location)
    }

    val type = "text/plain"
    val subject = post.restaurantName
    val extraText = AddressMap.getAddressFromLocation(
        context,
        post.location!!.latitude,
        post.location.longitude
    )

    val intent = Intent(Intent.ACTION_SEND)
    intent.type = type
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, extraText)
    if (post.photoList.isNotEmpty()) {
        intent.putExtra(Intent.EXTRA_STREAM, post.photoList.first().remoteUri)
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {

        Text(
            modifier = Modifier.weight(1f),
            text = AddressMap.getAddressFromLocation(
                context,
                post.location!!.latitude,
                post.location.longitude
            ),
            fontSize = 15.sp,
            color = Brown
        )
        IconButton(
            modifier = Modifier
                .padding(4.dp)
                .size(30.dp)
                .shadow(Shadows().small,
                    androidx.compose.material.Shapes().medium,
                    spotColor = Color.Gray,
                    ambientColor = Color.Transparent)
                .clip(RoundedCornerShape(5.dp))
                .background(Orange),
            onClick = {
                context.startActivity(shareIntent)
            }) {
            Icon(
                painter = painterResource(id = R.drawable.icon_location),
                contentDescription = stringResource(R.string.share),
                tint = Color.White
            )
        }
        IconButton(
            onClick = {
                context.startActivity(mapIntent)
            },
            modifier = Modifier
                .padding(5.dp)
                .size(30.dp)
                .shadow(Shadows().small,
                    androidx.compose.material.Shapes().medium,
                    spotColor = Color.Gray,
                    ambientColor = Color.Transparent)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_open_local_map),
                contentDescription = stringResource(R.string.open_map),
                tint = Orange
            )
        }
    }
}