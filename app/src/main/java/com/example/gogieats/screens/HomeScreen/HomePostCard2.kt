package com.example.gogieats.screens.HomeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Report
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gogieats.HomeScreenViewModel
import com.example.gogieats.R
import com.example.gogieats.data.firestore.Post
import com.example.gogieats.data.user.FullUser
import com.example.gogieats.data.user.User
import com.example.gogieats.screens.profile.ProfileViewModel
import com.example.gogieats.ui.theme.Yellow
import com.example.gogieats.util.BlockUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


@Composable
fun TopBox(
    post: Post,
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel?,
    postUser: MutableState<FullUser>
) {
    val context = LocalContext.current
    val currentUser = Firebase.auth.currentUser
    val openDialog = remember { mutableStateOf(false) }
    val viewModel = HomeScreenViewModel()

    if (openDialog.value) {
        if (profileViewModel == null) {
            AlertDialog(onDismissRequest = { openDialog.value = false },
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.report), color = Color.Red)
                        Icon(Icons.Rounded.Report,
                            contentDescription = stringResource(R.string.report),
                            tint = Color.Red)
                    }

                },
                text = {
                    Text("Please send an email to report this post.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            openDialog.value = false
                            val intent = viewModel.sendMail(
                                to = "hunter.krez@gmail.com",
                                subject = "Post Reported: User ID ${post.firebaseId}"
                            )
                            context.startActivity(intent)
                        }) {
                        Text(stringResource(R.string.go_to_email))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            openDialog.value = false
                        }) {
                        Text(stringResource(R.string.dismiss))
                    }
                })
        } else {
            AlertDialog(onDismissRequest = { openDialog.value = false },
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.delete_post_question),
                            color = Color.Red)
                        Icon(Icons.Rounded.Report,
                            contentDescription = stringResource(R.string.delete),
                            tint = Color.Red)
                    }

                },
                text = {
                    Text(stringResource(R.string.delete_warning))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            profileViewModel.delete(post.firebaseId, post)
                            openDialog.value = false
                        }) {
                        Text(stringResource(id = R.string.delete))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            openDialog.value = false
                        }) {
                        Text(stringResource(id = R.string.dismiss))
                    }
                })
        }
    }

    var expanded by remember { mutableStateOf(false) }
    val totalValue =
        post.valueAmenities + post.valueMeat + post.valueAtmosphere + post.valueSideDishes

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .background(Color.Black.copy(0.8f))
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .scale(1f)
                            .padding(end = 4.dp),
                        painter = painterResource(id = R.drawable.icon_star),
                        contentDescription = null,
                        tint = Yellow
                    )
                    Text(
                        text = totalValue.toString(),
                        style = MaterialTheme.typography.subtitle1,
                        fontSize = 22.sp,
                        color = Yellow
                    )
                }

            }
            Text(
                text = post.restaurantName,
                style = MaterialTheme.typography.subtitle1,
                fontSize = 22.sp,
                color = Color.White
            )
            IconButton(onClick = {
                expanded = true
            }) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.more),
                    tint = Color.White
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = {
                        if (profileViewModel == null) {
                            openDialog.value = true
                            expanded = false
                        } else {
                            profileViewModel.photoList.clear()
                            profileViewModel.toBeDeletedPhotoList.clear()
                            profileViewModel.post.value = post.deepCopy()
                            profileViewModel.editingPost =
                                profileViewModel.convertPostToEditingPost(profileViewModel.post.value)

                            post.photoList.forEach {
                                profileViewModel.editPhotoList.add(it)
                            }
                            profileViewModel.editingState.value = true
                            profileViewModel.changeLocation(post.location!!.latitude,
                                post.location.longitude,
                                context)
                            expanded = false
                        }
                    }) {
                        if (profileViewModel == null) {
                            Text(stringResource(R.string.report))
                        } else {
                            Text(text = stringResource(id = R.string.edit_post))
                        }
                    }
                    if (currentUser != null) {
                        DropdownMenuItem(
                            onClick = {
                                BlockUser.blockUser(currentUser.uid, User(
                                    uid = post.userId,
                                    profileAvatar = postUser.value.profile_avatar_remote_uri,
                                    userName = post.authorDisplayName
                                ))
                            }
                        ) {
                            Text(stringResource(R.string.block))
                        }
                    }
                    if (profileViewModel != null) {
                        DropdownMenuItem(onClick = {
                            openDialog.value = true
                            expanded = false
                        }) {
                            Text(stringResource(R.string.delete_post_confirmation))
                        }
                    }

                }
            }
        }
    }
}
