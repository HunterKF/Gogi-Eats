package com.example.kbbqreview.screens.HomeScreen

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kbbqreview.ApplicationViewModel
import com.example.kbbqreview.data.firestore.Post
import com.example.kbbqreview.R
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.items
import com.example.kbbqreview.screens.EditReview
import com.example.kbbqreview.screens.camera.CameraViewModel
import com.example.kbbqreview.screens.camera.ProfileCamera
import com.example.kbbqreview.screens.login.LoadingScreen
import com.example.kbbqreview.screens.map.location.LocationDetails
import com.example.kbbqreview.screens.profile.ProfilePostCard
import com.example.kbbqreview.screens.profile.ProfileScreenState
import com.example.kbbqreview.screens.profile.ProfileViewModel
import com.example.kbbqreview.ui.theme.Purple500
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavHostController,
    cameraViewModel: CameraViewModel,
    location: LocationDetails?,
    profileViewModel: ProfileViewModel,
    applicationViewModel: ApplicationViewModel,
    navigationToSignIn: () -> Unit,
) {
    val state by profileViewModel.state.collectAsState()
    val user by applicationViewModel.liveDateUser.observeAsState()
    val editing = profileViewModel.editingState
    val context = LocalContext.current
    val userNameAvailable = remember {
        mutableStateOf(true)
    }
    val userNameChecked = remember {
        mutableStateOf(false)
    }

    println("THE PAGE HAS LOADED!!! HOLD YOUR BUNS!!!")
    println("Current state of profile state: ${state}")
    LaunchedEffect(key1 = user) {
        println("Profile launched effect has occurred. Changing current user.")
        profileViewModel.setCurrentUser(user)
        profileViewModel.checkIfSignedIn()
    }

    Scaffold(
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    BottomNavigationItem(
                        icon = { Icon(screen.vector, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            if (screen.route == "profile") {
                                editing.value = false
                            }
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val displayName = profileViewModel.setDisplayName()
            val userNameState = remember {
                mutableStateOf(displayName)
            }
            val avatarUrl = profileViewModel.setAvatar()
            val profilePhotoState = remember {
                mutableStateOf(avatarUrl)
            }
            when (state) {
                is ProfileScreenState.Loaded -> {
                    val loaded = state as ProfileScreenState.Loaded

                    if (editing.value) {
                        EditReview(
                            navController = navController,
                            cameraViewModel = cameraViewModel,
                            profileViewModel = profileViewModel,
                            location = location
                        )
                    } else {
                        ProfileContent(
                            posts = loaded.posts,
                            avatarUrl = avatarUrl,
                            displayName = displayName,
                            profileViewModel = profileViewModel,
                            onEditClick = {
                                editing.value = true

                            },
                            onSignOut = {
                                profileViewModel.signOut()
                            }
                        )
                    }

                }
                ProfileScreenState.Camera -> {
                    ProfileCamera(cameraViewModel = cameraViewModel,
                        stateChange = { profileViewModel.changeToSettings() })
                }
                ProfileScreenState.Settings -> {
                    ProfileSettings(
                        cameraViewModel = cameraViewModel,
                        context = context,
                        avatarPhoto = avatarUrl,
                        viewModel = profileViewModel,
                        userNameAvailable = userNameAvailable,
                        userNameChecked = userNameChecked,
                        displayName = displayName,
                        navigateToProfile = { profileViewModel.checkIfSignedIn() }
                    )
                }
                ProfileScreenState.Loading -> LoadingScreen()
                ProfileScreenState.SignInRequired -> LaunchedEffect(key1 = Unit) {
                    if (profileViewModel.currentUser == null) {
                        navigationToSignIn()
                    } else {
                        profileViewModel.checkIfSignedIn()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileContent(
    posts: List<Post>,
    avatarUrl: Photo,
    onSignOut: () -> Unit,
    displayName: String,
    onEditClick: () -> Unit,
    profileViewModel: ProfileViewModel,
) {
    val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    val gridLayoutState = remember {
        mutableStateOf(true)
    }
    val scope = rememberCoroutineScope()
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.TopCenter),
                    onClick = { scope.launch { sheetState.collapse() } }) {
                    Icon(
                        Icons.Rounded.HorizontalRule,
                        contentDescription = "Close sheet",
                        modifier = Modifier
                            .scale(1.5f)
                            .offset(y = (-10).dp),
                        tint = Color.LightGray
                    )
                }

                Column(modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = { onSignOut() }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center) {
                                Text(text = "Sign out")
                                Spacer(modifier = Modifier.padding(5.dp))
                                Icon(
                                    Icons.Rounded.Logout,
                                    tint = Purple500,
                                    contentDescription = "Sign out"
                                )

                            }
                        }

                    }
                    Divider(Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = { profileViewModel.changeToSettings() }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center) {
                                Text(text = "Account settings")
                                Spacer(modifier = Modifier.padding(5.dp))
                                Icon(
                                    Icons.Rounded.Settings,
                                    tint = Purple500,
                                    contentDescription = "Account settings"
                                )

                            }
                        }

                    }
                }
            }
        },
        sheetPeekHeight = 0.dp,
        sheetGesturesEnabled = true,
        sheetShape = RoundedCornerShape(topEnd = 5.dp, topStart = 5.dp)

    ) {
        val size = posts.size
        val lazyColumnState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 40.dp),
            state = lazyColumnState
        ) {
            stickyHeader {
                UserBar(scope, sheetState, displayName, avatarUrl)
            }
            item {
                Column(
                    Modifier.padding(4.dp)
                ) {
                    StatsBar(size)
                    ViewSelector(gridLayoutState)
                }
            }
            if (gridLayoutState.value) {
                item {
                    FlowRow(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween
                    ) {
                        val itemSize: Dp = (LocalConfiguration.current.screenWidthDp.dp / 2)
                        var defaultIndex = 2
                        val context = LocalContext.current
                        posts.forEach { post ->
                            val index = defaultIndex
                            GridViewCard(
                                Modifier
                                    .size(itemSize)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .aspectRatio(1f),
                                post
                            ) {
                                gridLayoutState.value = false
                                scope.launch {
                                    lazyColumnState.scrollToItem(index, 0)
                                    Toast.makeText(context, "Index: $index", Toast.LENGTH_SHORT).show()
                                }
                            }
                            defaultIndex++
                        }
                    }
                }
            } else {
                itemsIndexed(posts) { index, post ->
                    SingleViewCard(post, onEditClick, profileViewModel = profileViewModel)
                }
            }
        }
    }
}


@Composable
private fun GridViewCard(modifier: Modifier, post: Post, onClick: () -> Job) {

    Box(
        modifier = modifier.clickable { onClick() }

    ) {
        val photoList by remember {
            mutableStateOf(post.photoList)
        }
        val emptyPhoto = Photo(
            "",
            "",
            "",
            0
        )
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(
                    data = if (photoList.isNotEmpty()) photoList[0].remoteUri else emptyPhoto
                )
                .placeholder(R.drawable.ic_image_placeholder)
                .crossfade(true)
                .build(),
            contentDescription = "",
            Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Scrim(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.BottomCenter)
        )
        Text(
            modifier = Modifier
                .padding(6.dp)
                .align(Alignment.BottomStart),
            text = post.restaurantName,
            color = Color.White
        )
    }
}


@OptIn(ExperimentalPagerApi::class)
@Composable
private fun SingleViewCard(
    post: Post,
    onEditClick: () -> Unit,
    profileViewModel: ProfileViewModel,
) {
    val state = rememberPagerState()
    ProfilePostCard(
        post = post,
        state = state,
        onEditClick = onEditClick,
        profileViewModel = profileViewModel
    )
}

@Composable
private fun ViewSelector(gridLayout: MutableState<Boolean>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { gridLayout.value = true }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_grid_view_24),
                contentDescription = "Grid view"
            )
        }
        IconButton(onClick = { gridLayout.value = false }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_crop_din_24),
                contentDescription = "Single view"
            )
        }
    }
}

@Composable
private fun StatsBar(size: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Text(
            text = "Total Reviews",
            style = MaterialTheme.typography.body1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Divider(Modifier
            .width(60.dp)
            .height(2.dp)
            .clip(RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.height(4.dp))
        Text(size.toString(),
            style = MaterialTheme.typography.h6)
    }
}

@Preview(showSystemUi = true)
@Composable
fun StatsPreview() {
    StatsBar(size = 5)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun UserBar(
    scope: CoroutineScope,
    sheetState: BottomSheetState,
    userName: String,
    avatarUrl: Photo,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        IconButton(modifier = Modifier.padding(8.dp), onClick = {
            Toast.makeText(context, avatarUrl.remoteUri, Toast.LENGTH_SHORT).show()
        }) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl.remoteUri)
                    .placeholder(R.drawable.profile)
                    .crossfade(true)
                    .build(), contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Blue, CircleShape)
            )
        }

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = userName,
            style = MaterialTheme.typography.h6
        )
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = { scope.launch { if (sheetState.isCollapsed) sheetState.expand() else sheetState.collapse() } }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_more),
                contentDescription = "Options"
            )
        }
    }
}

/*@Composable
private fun AdjustProfileSettings(
    cameraViewModel: CameraViewModel,
    context: Context,
    viewModel: LoginViewModel,
    userNameAvailable: MutableState<Boolean>,
    userNameChecked: MutableState<Boolean>,
    navigateToProfile: () -> Unit,
) {
    val profileViewModel = ProfileViewModel()
    Box(Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 40.dp)
            .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly) {
            val profilePhoto = cameraViewModel.getProfilePhoto()
            val avatarUrl = profileViewModel.setAvatar()
            val userNameState = remember {
                mutableStateOf("")
            }
            val currentCharCount = remember { mutableStateOf(0) }

            Box(Modifier
                .padding(top = 20.dp, bottom = 10.dp)
                .fillMaxWidth(0.4f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .border(4.dp, Color.Cyan, CircleShape)) {
                AsyncImage(modifier = Modifier.fillMaxSize(),
                    model = ImageRequest.Builder(context)
                        .data(profilePhoto?.localUri ?: avatarUrl)
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
                    IconButton(onClick = { viewModel.changeToSettingsCamera() }) {
                        Icon(
                            Icons.Rounded.PhotoCamera,
                            "Take profile picture",
                            tint = Color.Black,
                            modifier = Modifier.scale(1.3f)
                        )
                    }
                }
            }
            val maxChars = 15
            TextField(
                value = userNameState.value,
                onValueChange = { newValue ->
                    if (newValue.length <= maxChars) {
                        currentCharCount.value = newValue.length
                        viewModel.onTextFieldChange(
                            userNameState,
                            newValue
                        )
                    } else {
                        Toast.makeText(context, "Shorten name.", Toast.LENGTH_SHORT).show()
                    }
                },
                leadingIcon = { Icon(Icons.Rounded.DriveFileRenameOutline, null) },
                trailingIcon = {
                    IconButton(onClick = {
                        userNameAvailable.value =
                            viewModel.checkUserNameAvailability(userNameState.value,
                                userNameAvailable,
                                context)
                        if (!userNameAvailable.value) {
                            userNameChecked.value = true
                        }
                    }) {
                        Icon(Icons.Rounded.PersonSearch, null)
                    }
                },
                modifier = Modifier
                    .padding()
                    .border(
                        BorderStroke(width = 2.dp,
                            color = if (userNameAvailable.value) com.example.kbbqreview.screens.camera.ui.theme.Purple500 else Color.Red),
                        shape = RoundedCornerShape(50)
                    )
                    .fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Profile name",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.subtitle1
                    )
                },
                maxLines = 1,
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            val currentUser = Firebase.auth.currentUser
            Button(enabled = userNameState.value != "", onClick = {
                if (profilePhoto != null) {
                    viewModel.createNewAccount(currentUser = currentUser,
                        userName = userNameState.value,
                        context = context,
                        profilePhoto = profilePhoto,
                        navigateToHome = navigateToProfile)
                } else {
                    viewModel.createNewAccount(currentUser = currentUser,
                        userName = userNameState.value,
                        context = context,
                        profilePhoto = Photo(localUri = avatarUrl),
                        navigateToHome = navigateToProfile)
                }
            }) {
                Text("Complete sign in")
            }
        }
    }
}*/

@Composable
private fun ProfileSettings(
    cameraViewModel: CameraViewModel,
    context: Context,
    viewModel: ProfileViewModel,
    userNameAvailable: MutableState<Boolean>,
    userNameChecked: MutableState<Boolean>,
    navigateToProfile: () -> Unit,
    avatarPhoto: Photo,
    displayName: String,
) {
    val userNameState = remember {
        mutableStateOf(displayName)
    }

    Box(Modifier.fillMaxSize()) {
        IconButton(modifier = Modifier
            .align(Alignment.TopStart)
            .padding(4.dp),
            onClick = {
                navigateToProfile()
            }) {
            Icon(Icons.Rounded.Close, null)
        }
        Column(modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 40.dp)
            .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly) {
            val profilePhoto = cameraViewModel.getProfilePhoto()
            val currentCharCount = remember { mutableStateOf(0) }

            Box(Modifier
                .padding(top = 20.dp, bottom = 10.dp)
                .fillMaxWidth(0.4f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .border(4.dp, Color.Cyan, CircleShape)) {
                AsyncImage(modifier = Modifier.fillMaxSize(),
                    model = ImageRequest.Builder(context)
                        .data(profilePhoto?.localUri ?: avatarPhoto.remoteUri)
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
                    IconButton(onClick = { viewModel.changeToSettingsCamera() }) {
                        Icon(
                            Icons.Rounded.PhotoCamera,
                            "Take profile picture",
                            tint = Color.Black,
                            modifier = Modifier.scale(1.3f)
                        )
                    }
                }
            }
            val maxChars = 15
            TextField(
                value = userNameState.value,
                onValueChange = { newValue ->
                    if (newValue.length <= maxChars) {
                        currentCharCount.value = newValue.length
                        viewModel.onTextFieldChange(
                            userNameState,
                            newValue
                        )
                    } else {
                        Toast.makeText(context, "Shorten name.", Toast.LENGTH_SHORT).show()
                    }
                },
                leadingIcon = { Icon(Icons.Rounded.DriveFileRenameOutline, null) },
                trailingIcon = {
                    IconButton(onClick = {
                        userNameAvailable.value =
                            viewModel.checkUserNameAvailability(userNameState.value,
                                userNameAvailable,
                                context)
                        if (!userNameAvailable.value) {
                            userNameChecked.value = true
                        }
                    }) {
                        Icon(Icons.Rounded.PersonSearch, null)
                    }
                },
                modifier = Modifier
                    .padding()
                    .border(
                        BorderStroke(width = 2.dp,
                            color = if (userNameAvailable.value) com.example.kbbqreview.screens.camera.ui.theme.Purple500 else Color.Red),
                        shape = RoundedCornerShape(50)
                    )
                    .fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Profile name",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.subtitle1
                    )
                },
                maxLines = 1,
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            val currentUser = Firebase.auth.currentUser
            Button(enabled = userNameState.value != "", onClick = {
                viewModel.updateAccount(
                    currentUser = currentUser,
                    userName = userNameState.value,
                    newPhoto = profilePhoto,
                    oldPhoto = avatarPhoto,
                    navigateToHome = navigateToProfile
                )
            }) {
                Text("Update")
            }
        }
    }
    BackHandler() {
        navigateToProfile()
    }
}

@Composable
fun Scrim(modifier: Modifier) {
    //This is to make the bottom of the photo cards a little more readable
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                listOf(Color.Transparent, Color(0x99000000))
            )
        )
    )
}