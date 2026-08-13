package com.shadow.shineyverse.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shadow.shineyverse.ui.common.BrandWordmark
import com.shadow.shineyverse.ui.common.PipFab
import com.shadow.shineyverse.ui.gallery.GalleryScreen
import com.shadow.shineyverse.ui.generate.GenerateScreen
import com.shadow.shineyverse.ui.settings.SettingsScreen

private data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab(Destinations.MAIN_CREATE, "Create", Icons.Filled.Add),
    BottomTab(Destinations.MAIN_GALLERY, "Gallery", Icons.Filled.PhotoLibrary),
    BottomTab(Destinations.MAIN_SETTINGS, "Settings", Icons.Filled.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    onLoggedOut: () -> Unit,
    onEditServerUrl: () -> Unit,
    onOpenItemDetail: () -> Unit,
    onOpenPip: () -> Unit,
) {
    val tabNavController = rememberNavController()
    val backStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.hierarchy?.firstOrNull()?.route

    Scaffold(
        topBar = {
            TopAppBar(title = { BrandWordmark() })
        },
        floatingActionButton = {
            PipFab(onClick = onOpenPip, icon = Icons.Filled.AutoAwesome)
        },
        bottomBar = {
            NavigationBar {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = Destinations.MAIN_CREATE,
            modifier = Modifier.padding(padding),
        ) {
            composable(Destinations.MAIN_CREATE) { GenerateScreen() }
            composable(Destinations.MAIN_GALLERY) {
                GalleryScreen(onNavigateToDetail = onOpenItemDetail)
            }
            composable(Destinations.MAIN_SETTINGS) {
                SettingsScreen(onLoggedOut = onLoggedOut, onEditServerUrl = onEditServerUrl)
            }
        }
    }
}
