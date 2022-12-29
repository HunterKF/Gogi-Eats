package com.example.gogieats.screens.HomeScreen

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gogieats.ApplicationViewModel
import com.example.gogieats.data.firestore.Post
import com.example.gogieats.R
import com.example.gogieats.data.photos.Photo
import com.example.gogieats.items
import com.example.gogieats.screens.EditReview
import com.example.gogieats.screens.camera.CameraViewModel
import com.example.gogieats.screens.camera.ProfileCamera
import com.example.gogieats.screens.login.LoadingScreen
import com.example.gogieats.screens.login.util.CustomTextField
import com.example.gogieats.screens.login.util.DividedBackground
import com.example.gogieats.screens.map.location.LocationDetails
import com.example.gogieats.screens.profile.ProfileScreenState
import com.example.gogieats.screens.profile.ProfileViewModel
import com.example.gogieats.screens.util.OrangeButton
import com.example.gogieats.ui.theme.Brown
import com.example.gogieats.ui.theme.Orange
import com.example.gogieats.ui.theme.Shadows
import com.example.gogieats.ui.theme.Yellow
import com.example.gogieats.util.UserViewModel
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var label = ""
    items.forEach {
        if (currentDestination?.route == it.route) {
            label = it.label
        }
    }
    val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .shadow(12.dp, RoundedCornerShape(0.dp), spotColor = Color.Black),
                backgroundColor = Color.White,
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable._12_512),
                            null
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.h6,
                            color = Brown
                        )
                        IconButton(
                            modifier = Modifier,
                            onClick = { scope.launch { if (sheetState.isCollapsed) sheetState.expand() else sheetState.collapse() } }) {
                            Icon(
                                Icons.Rounded.MoreVert,
                                contentDescription = stringResource(R.string.options)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(0.dp), spotColor = Color.Black),
                backgroundColor = Color.White
            ) {
                items.forEach { screen ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                modifier = Modifier.size(22.dp),
                                painter = painterResource(id = screen.icon
                                    ?: R.drawable.icon_meat),
                                contentDescription = null,
                                tint = if (currentDestination?.route == screen.route) Orange else Color.LightGray)
                        },
                        label = {
                            Text(
                                text = screen.label,
                                style = MaterialTheme.typography.subtitle2,
                                color = if (currentDestination?.route == screen.route) Orange else Color.LightGray)
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
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
        val padding = innerPadding
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val displayName = profileViewModel.setDisplayName()
            val avatarUrl = profileViewModel.setAvatar()
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
                            sheetState = sheetState,
                            scaffoldState = scaffoldState,
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
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        DividedBackground(modifier = Modifier.fillMaxSize())

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
    sheetState: BottomSheetState,
    scaffoldState: BottomSheetScaffoldState,
    padding: Dp = 24.dp,
) {

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
                    .height(200.dp)
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.TopCenter),
                    onClick = { scope.launch { sheetState.collapse() } }) {
                    Icon(
                        Icons.Rounded.HorizontalRule,
                        contentDescription = null,
                        modifier = Modifier
                            .scale(1.5f)
                            .offset(y = (-10).dp),
                        tint = Color.LightGray
                    )
                }

                Column(modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp)
                    .fillMaxWidth(0.6f)) {

                    TextButton(onClick = { onSignOut() }) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = stringResource(R.string.sign_out),
                                fontSize = 16.sp,
                                color = Brown)
                            Spacer(modifier = Modifier.padding(5.dp))
                            Icon(
                                Icons.Rounded.Logout,
                                tint = Brown,
                                contentDescription = stringResource(R.string.sign_out)
                            )

                        }


                    }
                    Divider(Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp))

                    TextButton(onClick = { profileViewModel.changeToSettings() }) {
                        Row(Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = stringResource(R.string.account_settings),
                                fontSize = 16.sp,
                                color = Brown)
                            Spacer(modifier = Modifier.padding(5.dp))
                            Icon(
                                Icons.Rounded.Settings,
                                tint = Brown,
                                contentDescription = stringResource(R.string.account_settings)
                            )
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
        val width: Dp by animateDpAsState(
            if (gridLayoutState.value)
                24.dp
            else
                0.dp
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 40.dp, top = 24.dp, start = 0.dp, end = 0.dp),
            state = lazyColumnState
        ) {
            item {
                UserBar(displayName, avatarUrl, padding = padding)
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                StatsBar(size, padding = padding)
            }
            item {
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                ViewSelector(gridLayoutState, padding)
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (gridLayoutState.value) {
                item {
                    FlowRow(
                        modifier = Modifier.padding(start = padding, end = padding, bottom = 40.dp),
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
                        crossAxisSpacing = 10.dp,
                        mainAxisSpacing = 10.dp
                    ) {
                        val itemSize: Dp =
                            ((LocalConfiguration.current.screenWidthDp.dp - 58.dp) / 2)
                        var defaultIndex = 2
                        posts.forEach { post ->
                            val index = defaultIndex
                            GridViewCard(
                                Modifier
                                    .width(itemSize)
                                    .aspectRatio(1.3f),
                                post
                            ) {
                                gridLayoutState.value = false
                                scope.launch {
                                    lazyColumnState.scrollToItem(index, -220)
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
        com.example.gogieats.screens.util.BlackScrim(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.BottomCenter)
        )
        Text(
            modifier = Modifier
                .padding(6.dp)
                .align(Alignment.Center),
            text = post.restaurantName,
            fontSize = 20.sp,
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

    val userViewModel = UserViewModel()
    LaunchedEffect(key1 = Unit, block = {
        userViewModel.getUser(post.userId)
    })
    val photoList by remember {
        mutableStateOf(post.photoList)
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        val state = rememberPagerState()
        Column {
            HomePostCard(state = state,
                post = post,
                photoList = photoList,
                modifier = Modifier.padding(bottom = 12.dp),
                postUser = userViewModel.user,
                profileViewModel = profileViewModel
            )
        }
    }
}

@Composable
private fun ViewSelector(gridLayout: MutableState<Boolean>, padding: Dp) {
    Column(modifier = Modifier
        .padding(horizontal = padding)
        .fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { gridLayout.value = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_grid_view),
                    contentDescription = stringResource(R.string.view_grid),
                    tint = Brown
                )
            }
            Divider(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp),
                color = Color.LightGray
            )
            IconButton(onClick = { gridLayout.value = false }) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_single_view),
                    contentDescription = stringResource(R.string.view_single),
                    tint = Brown
                )
            }

        }
        Divider(
            modifier = Modifier
                .fillMaxWidth(),
            color = Color.LightGray
        )
    }

}

@Composable
private fun StatsBar(size: Int, modifier: Modifier = Modifier, padding: Dp) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .padding(horizontal = padding)
            .fillMaxWidth()
            .background(Color.White)
            .shadow(Shadows().small, RoundedCornerShape(10.dp),
                spotColor = Color.Gray,
                ambientColor = Color.Transparent)
    ) {
        Row(Modifier.padding(vertical = 6.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround) {
            Text(
                text = "Total Reviews",
                fontSize = 22.sp,
                color = Orange
            )
            Divider(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp),
                color = Color.LightGray
            )

            Text(
                text = if (size <= 9) "0${size}" else size.toString(),
                fontSize = 30.sp,
                color = Orange
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun UserBar(
    userName: String,
    avatarUrl: Photo,
    padding: Dp,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl.remoteUri)
                .placeholder(R.drawable.profile)
                .crossfade(true)
                .build(), contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .aspectRatio(1f)
                .clip(CircleShape)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = userName,
            style = MaterialTheme.typography.h6,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Brown
        )

    }
}

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
    modifier: Modifier = Modifier,
) {
    val userNameState = remember {
        mutableStateOf(displayName)
    }
    val focusManager = LocalFocusManager.current


    Card(
        modifier = modifier
            .padding(12.dp)
            .shadow(4.dp,
                RoundedCornerShape(15.dp),
                spotColor = Color.LightGray,
                ambientColor = Color.Transparent)
            .clip(RoundedCornerShape(15.dp))
            .zIndex(1f)
    ) {
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
                .padding(horizontal = 24.dp, vertical = 40.dp)
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
                    .clickable {
                        viewModel.changeToSettingsCamera()
                    }
                    .border(2.dp, Yellow, CircleShape)) {
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
                        .fillMaxSize()
                        .background(Color.White.copy(0.2f)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.PhotoCamera,
                            stringResource(R.string.take_profile_photo),
                            tint = Yellow,
                            modifier = Modifier.scale(1.3f)
                        )
                    }
                }
                val maxChars = 15


                CustomTextField(icon = Icons.Outlined.Person,
                    text = "Username",
                    label = "e.g.maxpaene",
                    value = userNameState,
                    modifier = modifier,
                    maxChars = maxChars,
                    currentCharCount = currentCharCount,
                    context = context,
                    focusManager = focusManager,
                    isUserName = true,
                    profileViewModel = viewModel,
                    userNameAvailable = userNameAvailable,
                    userNameChecked = userNameChecked,
                    keyboardType = KeyboardType.Password
                )
                val currentUser = Firebase.auth.currentUser

                OrangeButton(
                    text = stringResource(id = R.string.update),
                    onClick = {
                        viewModel.updateAccount(
                            currentUser = currentUser,
                            userName = userNameState.value,
                            newPhoto = profilePhoto,
                            oldPhoto = avatarPhoto,
                            navigateToHome = navigateToProfile
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true
                )
            }
        }
    }
    BackHandler() {
        navigateToProfile()
    }
}
