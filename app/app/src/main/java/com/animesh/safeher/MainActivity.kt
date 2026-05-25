package com.animesh.safeher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.animesh.safeher.app.ui.screens.ContactsScreen
import com.animesh.safeher.ui.Screen
import com.animesh.safeher.ui.screens.*
import com.animesh.safeher.ui.theme.SafeHerTheme
import com.animesh.safeher.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeHerTheme {
                SafeHerApp()
            }
        }
    }
}

data class BottomNavItem(val screen: Screen, val icon: ImageVector, val label: String)

@Composable
fun SafeHerApp() {
    val viewModel: MainViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home, Icons.Default.Home, "Home"),
        BottomNavItem(Screen.Map, Icons.Default.Map, "Map"),
        BottomNavItem(Screen.Contacts, Icons.Default.Contacts, "Contacts"),
        BottomNavItem(Screen.Profile, Icons.Default.Person, "Profile"),
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route, Screen.Map.route,
        Screen.Contacts.route, Screen.Profile.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = com.animesh.safeher.ui.theme.SurfaceDark,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = com.animesh.safeher.ui.theme.PinkPrimary,
                                selectedTextColor = com.animesh.safeher.ui.theme.PinkPrimary,
                                unselectedIconColor = com.animesh.safeher.ui.theme.OnSurface.copy(0.5f),
                                unselectedTextColor = com.animesh.safeher.ui.theme.OnSurface.copy(0.5f),
                                indicatorColor = com.animesh.safeher.ui.theme.PinkPrimary.copy(0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (uiState.isLoggedIn) Screen.Home.route else Screen.Login.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel)
            }

            composable(Screen.Map.route) {
                MapScreen(viewModel = viewModel)
            }

            composable(Screen.Contacts.route) {
                ContactsScreen(viewModel = viewModel)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onSignOut = {
                        viewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}