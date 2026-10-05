package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.*
import com.example.location.LocationTrackerScreen
import com.example.ui.screens.SavingsAdvisorScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AppNavigationContent(viewModel: MainViewModel) {
    val isAppLoading by viewModel.isAppLoading.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isCoupled by viewModel.isCoupled.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val showSavingsCoach by viewModel.showSavingsCoach.collectAsStateWithLifecycle()

    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = isAppLoading,
        transitionSpec = {
            if (targetState == false) {
                (fadeIn(animationSpec = tween(750, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 1.04f, animationSpec = tween(750, easing = FastOutSlowInEasing)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(450, easing = FastOutSlowInEasing)) +
                            scaleOut(targetScale = 0.96f, animationSpec = tween(450, easing = FastOutSlowInEasing))
                    )
            } else {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(450))
            }
        },
        label = "LoadingTransition"
    ) { loading ->
        when {
            loading -> LoadingScreen(viewModel)
            !isLoggedIn -> LoginScreen(viewModel)
            !isCoupled -> CoupleConnectScreen(viewModel)
            showSavingsCoach -> SavingsAdvisorScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.closeSavingsCoach() }
            )
            else -> AnimatedContent(
                modifier = Modifier.fillMaxSize(),
                targetState = selectedTab,
                transitionSpec = {
                    val duration = 320
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = tween(duration, easing = FastOutSlowInEasing)) { width ->
                            (width * 0.12f).toInt()
                        } + fadeIn(animationSpec = tween(duration, easing = FastOutSlowInEasing)))
                            .togetherWith(
                                slideOutHorizontally(animationSpec = tween(duration - 50, easing = FastOutSlowInEasing)) { width ->
                                    (-width * 0.12f).toInt()
                                } + fadeOut(animationSpec = tween(duration - 50, easing = FastOutSlowInEasing))
                            )
                    } else {
                        (slideInHorizontally(animationSpec = tween(duration, easing = FastOutSlowInEasing)) { width ->
                            (-width * 0.12f).toInt()
                        } + fadeIn(animationSpec = tween(duration, easing = FastOutSlowInEasing)))
                            .togetherWith(
                                slideOutHorizontally(animationSpec = tween(duration - 50, easing = FastOutSlowInEasing)) { width ->
                                    (width * 0.12f).toInt()
                                } + fadeOut(animationSpec = tween(duration - 50, easing = FastOutSlowInEasing))
                            )
                    }
                },
                label = "TabTransition"
            ) { tab ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (tab) {
                        0 -> DashboardScreen(viewModel)
                        1 -> LearningScreen(viewModel)
                        2 -> ExpensesScreen(viewModel)
                        3 -> LocationTrackerScreen(viewModel)
                        4 -> ProfilesScreen(viewModel)
                    }
                }
            }
        }
    }
}
