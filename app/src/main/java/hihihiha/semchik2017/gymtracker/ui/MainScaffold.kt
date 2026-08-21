package hihihiha.semchik2017.gymtracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import hihihiha.semchik2017.gymtracker.R
import hihihiha.semchik2017.gymtracker.ui.navigation.BottomNavItem
import hihihiha.semchik2017.gymtracker.ui.navigation.NavGraph
import hihihiha.semchik2017.gymtracker.ui.navigation.Screen

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val navItems = listOf(
        BottomNavItem(Screen.Workouts, R.string.nav_workouts, Icons.Default.FitnessCenter),
        BottomNavItem(Screen.Exercises, R.string.nav_exercises, Icons.AutoMirrored.Filled.List),
        BottomNavItem(Screen.Nutrition, R.string.nav_nutrition, Icons.Default.Restaurant),
        BottomNavItem(Screen.Weight, R.string.nav_weight, Icons.Default.Person)
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isBottomBarVisible = navItems.any { item ->
        currentDestination?.hasRoute(item.route::class) == true
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isBottomBarVisible,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_app_settings)) },
                    selected = currentDestination?.hasRoute(Screen.AppSettings::class) == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.AppSettings)
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_backup)) },
                    selected = currentDestination?.hasRoute(Screen.Backup::class) == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Backup)
                    },
                    icon = { Icon(Icons.Default.Storage, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_about)) },
                    selected = currentDestination?.hasRoute(Screen.About::class) == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.About)
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (isBottomBarVisible) {
                    NavigationBar {
                        navItems.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = null) },
                                label = { Text(stringResource(item.titleRes)) },
                                selected = currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                NavGraph(navController = navController)
            }
        }
    }
}
