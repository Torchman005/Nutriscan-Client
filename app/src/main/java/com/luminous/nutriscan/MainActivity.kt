package com.example.foodnutritionaiassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodnutritionaiassistant.ui.analysis.AnalysisScreen
import com.example.foodnutritionaiassistant.ui.screens.*
import com.example.foodnutritionaiassistant.ui.theme.FoodNutritionAIAssistantTheme
import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel
import com.example.foodnutritionaiassistant.ui.viewmodel.FoodAnalysisViewModel
import com.example.foodnutritionaiassistant.data.repository.FoodAnalysisRepository
import com.example.foodnutritionaiassistant.data.network.RetrofitClient
import androidx.compose.ui.platform.LocalContext
import com.luminous.nutriscan.ui.screens.AnalysisResultScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodNutritionAIAssistantTheme {
                FoodNutritionApp()
            }
        }
    }
}

enum class RegistrationStep {
    LOGIN,
    REGISTER,
    NICKNAME,
    GENDER_AGE,
    GROUP_SELECTION,
    HEIGHT_WEIGHT,
    REGISTRATION_DONE,
    COMPLETED
}

@Composable
fun FoodNutritionApp() {
    val userViewModel: UserViewModel = viewModel()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        userViewModel.checkLoginStatus(context)
    }
    
    val foodAnalysisRepository = remember { FoodAnalysisRepository(RetrofitClient.apiService) }
    val foodAnalysisViewModel: FoodAnalysisViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FoodAnalysisViewModel(foodAnalysisRepository) as T
        }
    })
    
    var registrationStep by rememberSaveable { mutableStateOf(RegistrationStep.LOGIN) }
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    if (userViewModel.isCheckingLogin) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF66BB6A))
        }
        return
    }

    LaunchedEffect(userViewModel.isLoggedIn) {
        if (userViewModel.isLoggedIn) {
            // Only jump if we are currently in the login flow (LOGIN or NICKNAME etc before COMPLETED)
            if (registrationStep != RegistrationStep.COMPLETED) {
                registrationStep = RegistrationStep.COMPLETED
                currentDestination = AppDestinations.HOME
            }
        } else {
            // If not logged in, ensure we are at LOGIN step if we were COMPLETED (logout case)
            if (registrationStep == RegistrationStep.COMPLETED) {
                registrationStep = RegistrationStep.LOGIN
            }
        }
    }

    BackHandler(enabled = registrationStep != RegistrationStep.LOGIN && registrationStep != RegistrationStep.COMPLETED) {
        registrationStep = when (registrationStep) {
            RegistrationStep.REGISTER -> RegistrationStep.LOGIN
            RegistrationStep.NICKNAME -> RegistrationStep.LOGIN
            RegistrationStep.GENDER_AGE -> RegistrationStep.NICKNAME
            RegistrationStep.GROUP_SELECTION -> RegistrationStep.GENDER_AGE
            RegistrationStep.HEIGHT_WEIGHT -> RegistrationStep.GROUP_SELECTION
            RegistrationStep.REGISTRATION_DONE -> RegistrationStep.HEIGHT_WEIGHT
            else -> registrationStep
        }
    }

    AnimatedContent(
        targetState = registrationStep,
        transitionSpec = {
            if (targetState.ordinal > initialState.ordinal) {
                slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn(animationSpec = tween(300)) togetherWith
                        slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut(animationSpec = tween(300))
            } else {
                slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn(animationSpec = tween(300)) togetherWith
                        slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut(animationSpec = tween(300))
            }
        },
        label = "RegistrationTransition"
    ) { step ->
        when (step) {
            RegistrationStep.LOGIN -> {
                LoginScreen(
                    viewModel = userViewModel,
                    onLoginSuccess = { isFirstLogin ->
                        if (isFirstLogin) {
                            registrationStep = RegistrationStep.NICKNAME
                        } else {
                            registrationStep = RegistrationStep.COMPLETED
                            currentDestination = AppDestinations.HOME
                        }
                    },
                    onSkip = {
                        registrationStep = RegistrationStep.COMPLETED
                        currentDestination = AppDestinations.HOME
                    },
                    onRegisterClick = {
                        registrationStep = RegistrationStep.REGISTER
                    }
                )
            }
            RegistrationStep.REGISTER -> {
                RegisterScreen(
                    viewModel = userViewModel,
                    onRegisterSuccess = { registrationStep = RegistrationStep.NICKNAME },
                    onBack = { registrationStep = RegistrationStep.LOGIN }
                )
            }
            RegistrationStep.NICKNAME -> {
                NicknameScreen(
                    viewModel = userViewModel,
                    onNext = { registrationStep = RegistrationStep.GENDER_AGE }
                )
            }
            RegistrationStep.GENDER_AGE -> {
                GenderAgeScreen(
                    viewModel = userViewModel,
                    onNext = { registrationStep = RegistrationStep.GROUP_SELECTION },
                    onBack = { registrationStep = RegistrationStep.NICKNAME }
                )
            }
            RegistrationStep.GROUP_SELECTION -> {
                GroupSelectionScreen(
                    viewModel = userViewModel,
                    onNext = { registrationStep = RegistrationStep.HEIGHT_WEIGHT },
                    onBack = { registrationStep = RegistrationStep.GENDER_AGE }
                )
            }
            RegistrationStep.HEIGHT_WEIGHT -> {
                HeightWeightScreen(
                    viewModel = userViewModel,
                    onFinish = {
                        registrationStep = RegistrationStep.REGISTRATION_DONE
                    },
                    onBack = { registrationStep = RegistrationStep.GROUP_SELECTION }
                )
            }
            RegistrationStep.REGISTRATION_DONE -> {
                RegistrationCompleteScreen(
                    onDone = {
                        registrationStep = RegistrationStep.COMPLETED
                        currentDestination = AppDestinations.HOME
                    }
                )
            }
            RegistrationStep.COMPLETED -> {
                MainScreen(
                    currentDestination = currentDestination,
                    onDestinationChanged = { currentDestination = it },
                    isLoggedIn = userViewModel.isLoggedIn,
                    onLoginClick = { 
                        userViewModel.logout(context)
                        registrationStep = RegistrationStep.LOGIN 
                    },
                    userViewModel = userViewModel,
                    foodAnalysisViewModel = foodAnalysisViewModel
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    currentDestination: AppDestinations,
    onDestinationChanged: (AppDestinations) -> Unit,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    userViewModel: UserViewModel,
    foodAnalysisViewModel: FoodAnalysisViewModel
) {
    BackHandler(enabled = currentDestination != AppDestinations.HOME) {
        onDestinationChanged(AppDestinations.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color.Gray
            ) {
                AppDestinations.entries.filter { it.showInBottomBar }.forEach { destination ->
                    val isSelected = currentDestination == destination
                    val isCamera = destination == AppDestinations.CAMERA
                    
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onDestinationChanged(destination) },
                        icon = {
                            if (isCamera) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.label,
                                        tint = Color.White
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label,
                                    tint = if (isSelected) Color(0xFF2E7D32) else Color.Gray
                                )
                            }
                        },
                        label = {
                            if (!isCamera) {
                                Text(
                                    text = destination.label,
                                    color = if (isSelected) Color(0xFF2E7D32) else Color.Gray
                                )
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF1F8E9),
                            Color(0xFFFFFFFF)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        // Sliding to right (e.g. Home -> Profile)
                        slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn(animationSpec = tween(300)) togetherWith
                                slideOutHorizontally(animationSpec = tween(300)) { width -> -width/2 } + fadeOut(animationSpec = tween(300))
                    } else {
                        // Sliding to left (e.g. Profile -> Home)
                        slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn(animationSpec = tween(300)) togetherWith
                                slideOutHorizontally(animationSpec = tween(300)) { width -> width/2 } + fadeOut(animationSpec = tween(300))
                    }.using(
                        SizeTransform(clip = false)
                    )
                },
                label = "MainContentTransition"
            ) { destination ->
                when (destination) {
                    AppDestinations.HOME -> HomeScreen(isLoggedIn, onLoginClick, userViewModel)
                    AppDestinations.GROUP -> CommunityScreen(userViewModel = userViewModel)
                    AppDestinations.CAMERA -> ScanScreen(
                        viewModel = foodAnalysisViewModel,
                        userViewModel = userViewModel,
                        onHistoryClick = { onDestinationChanged(AppDestinations.HISTORY) },
                        onAnalysisComplete = { onDestinationChanged(AppDestinations.RESULT) }
                    )
                    AppDestinations.ANALYSIS -> AnalysisScreen(userViewModel)
                    AppDestinations.ME -> ProfileScreen(userViewModel, onLoginClick = onLoginClick)
                    AppDestinations.RESULT -> AnalysisResultScreen(foodAnalysisViewModel, userViewModel) { 
                        foodAnalysisViewModel.clearResult()
                        onDestinationChanged(AppDestinations.CAMERA) 
                    }
                    AppDestinations.HISTORY -> HistoryScreen(
                        viewModel = foodAnalysisViewModel,
                        userViewModel = userViewModel,
                        onBack = { onDestinationChanged(AppDestinations.CAMERA) },
                        onAnalysisComplete = { onDestinationChanged(AppDestinations.RESULT) }
                    )
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val showInBottomBar: Boolean = true
) {
    HOME("首页", Icons.Default.Home),
    GROUP("群体", Icons.Default.Person), // Using Person as group placeholder
    CAMERA("识别", Icons.Filled.PhotoCamera),
    ANALYSIS("分析", Icons.Default.DateRange),
    ME("我的", Icons.Default.AccountCircle),
    RESULT("结果", Icons.Default.Info, false),
    HISTORY("历史", Icons.Default.Info, false)
}
