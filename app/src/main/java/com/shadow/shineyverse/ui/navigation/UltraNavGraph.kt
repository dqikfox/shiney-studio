package com.shadow.shineyverse.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shadow.shineyverse.ui.auth.LoginScreen
import com.shadow.shineyverse.ui.auth.RegisterScreen
import com.shadow.shineyverse.ui.common.FullScreenLoading
import com.shadow.shineyverse.ui.itemdetail.ItemDetailScreen
import com.shadow.shineyverse.ui.pip.PipScreen
import com.shadow.shineyverse.ui.settings.ServerUrlScreen

@Composable
fun UltraNavGraph() {
    val startupViewModel: StartupViewModel = hiltViewModel()
    val startDestination by startupViewModel.startDestination.collectAsStateWithLifecycle()

    when (val destination = startDestination) {
        StartupViewModel.StartDestination.Loading -> FullScreenLoading(modifier = Modifier.fillMaxSize())
        else -> {
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                startupViewModel.sessionExpired.collect {
                    startupViewModel.onSessionExpiredHandled()
                    navController.navigateAndClearBackStack(Destinations.LOGIN)
                }
            }

            NavHost(
                navController = navController,
                startDestination = destination.toRoute(),
            ) {
                composable(Destinations.SERVER_SETUP) {
                    ServerUrlScreen(
                        onContinue = { navController.navigate(Destinations.LOGIN) },
                    )
                }
                composable(Destinations.LOGIN) {
                    LoginScreen(
                        onLoggedIn = { navController.navigateAndClearBackStack(Destinations.MAIN) },
                        onNavigateToRegister = { navController.navigate(Destinations.REGISTER) },
                        onEditServerUrl = { navController.navigate(Destinations.SERVER_SETUP) },
                    )
                }
                composable(Destinations.REGISTER) {
                    RegisterScreen(
                        onRegistered = { navController.navigateAndClearBackStack(Destinations.MAIN) },
                        onBackToLogin = { navController.popBackStack() },
                    )
                }
                composable(Destinations.MAIN) {
                    MainScaffold(
                        onLoggedOut = { navController.navigateAndClearBackStack(Destinations.LOGIN) },
                        onEditServerUrl = { navController.navigate(Destinations.SERVER_SETUP) },
                        onOpenItemDetail = { navController.navigate(Destinations.ITEM_DETAIL) },
                        onOpenPip = { navController.navigate(Destinations.PIP) },
                    )
                }
                composable(Destinations.ITEM_DETAIL) {
                    ItemDetailScreen(
                        onBack = { navController.popBackStack() },
                        onRemixed = { navController.popBackStack() },
                    )
                }
                composable(Destinations.PIP) {
                    PipScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

private fun StartupViewModel.StartDestination.toRoute(): String = when (this) {
    StartupViewModel.StartDestination.Loading -> Destinations.SERVER_SETUP // unreachable, guarded above
    StartupViewModel.StartDestination.ServerSetup -> Destinations.SERVER_SETUP
    StartupViewModel.StartDestination.Login -> Destinations.LOGIN
    StartupViewModel.StartDestination.Home -> Destinations.MAIN
}

private fun NavHostController.navigateAndClearBackStack(route: String) {
    navigate(route) {
        popUpTo(0) { inclusive = true }
    }
}
