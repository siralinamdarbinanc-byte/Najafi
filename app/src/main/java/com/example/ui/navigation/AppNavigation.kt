package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DriversScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MissionDetailScreen
import com.example.ui.screens.NewMissionScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.viewmodel.AppViewModelFactory
import com.example.viewmodel.DashboardViewModel
import com.example.viewmodel.DetailViewModel
import com.example.viewmodel.DriverViewModel
import com.example.viewmodel.HistoryViewModel
import com.example.viewmodel.NewMissionViewModel
import com.example.viewmodel.ReportViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "داشبورد", Icons.Default.Dashboard)
    object NewMission : Screen("new_mission?deliveryId={deliveryId}", "ثبت مأموریت", Icons.Default.AddCircle)
    object History : Screen("history", "سوابق", Icons.Default.History)
    object Drivers : Screen("drivers", "پیک‌ها", Icons.Default.DirectionsBike)
    object Reports : Screen("reports", "گزارش‌ها", Icons.Default.Analytics)
    object Settings : Screen("settings", "تنظیمات", Icons.Default.Dashboard)
    object Detail : Screen("detail/{deliveryId}", "جزئیات", Icons.Default.Dashboard)
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val factory = AppViewModelFactory(context)

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.NewMission,
        Screen.History,
        Screen.Drivers,
        Screen.Reports
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomBar = currentRoute in listOf(
            Screen.Dashboard.route,
            Screen.History.route,
            Screen.Drivers.route,
            Screen.Reports.route
        )

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    val targetRoute = if (screen == Screen.NewMission) {
                                        "new_mission?deliveryId=-1"
                                    } else {
                                        screen.route
                                    }
                                    navController.navigate(targetRoute) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Dashboard.route) {
                    val viewModel: DashboardViewModel = viewModel(factory = factory)
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToNewMission = { navController.navigate("new_mission?deliveryId=-1") },
                        onNavigateToHistory = { navController.navigate(Screen.History.route) },
                        onNavigateToDrivers = { navController.navigate(Screen.Drivers.route) },
                        onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToDetail = { id -> navController.navigate("detail/$id") }
                    )
                }

                composable(
                    route = Screen.NewMission.route,
                    arguments = listOf(
                        navArgument("deliveryId") {
                            type = NavType.LongType
                            defaultValue = -1L
                        }
                    )
                ) { backStackEntry ->
                    val deliveryId = backStackEntry.arguments?.getLong("deliveryId") ?: -1L
                    val viewModel: NewMissionViewModel = viewModel(factory = factory)
                    NewMissionScreen(
                        viewModel = viewModel,
                        deliveryIdToEdit = if (deliveryId > 0) deliveryId else null,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.History.route) {
                    val viewModel: HistoryViewModel = viewModel(factory = factory)
                    HistoryScreen(
                        viewModel = viewModel,
                        onNavigateToDetail = { id -> navController.navigate("detail/$id") },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.Detail.route,
                    arguments = listOf(
                        navArgument("deliveryId") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    val deliveryId = backStackEntry.arguments?.getLong("deliveryId") ?: 0L
                    val viewModel: DetailViewModel = viewModel(factory = factory)
                    MissionDetailScreen(
                        viewModel = viewModel,
                        deliveryId = deliveryId,
                        onNavigateToEdit = { id -> navController.navigate("new_mission?deliveryId=$id") },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Drivers.route) {
                    val viewModel: DriverViewModel = viewModel(factory = factory)
                    DriversScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Reports.route) {
                    val viewModel: ReportViewModel = viewModel(factory = factory)
                    ReportsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
