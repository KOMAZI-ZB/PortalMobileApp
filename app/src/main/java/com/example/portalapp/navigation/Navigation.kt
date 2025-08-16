package com.example.portalapp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.portalapp.views.notifications.NotificationsScreen
import com.example.portalapp.views.faq.FaqScreen // ⬅️ added
import kotlinx.coroutines.launch

// ----- Routes -----
private sealed class Dest(val route: String, val label: String) {
    data object Notifications : Dest("notifications", "Notifications")
    data object Modules       : Dest("modules",       "Modules")
    data object Scheduler     : Dest("scheduler",     "Scheduler")
    data object Repository    : Dest("repository",    "Repository")
    data object Faq           : Dest("faq",           "FAQ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val items = listOf(
        Dest.Notifications,
        Dest.Modules,
        Dest.Scheduler,
        Dest.Repository,
        Dest.Faq
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "CSI Portal",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                items.forEach { dest ->
                    NavigationDrawerItem(
                        label = { Text(dest.label) },
                        selected = currentRoute == dest.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentTitle(currentRoute)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Text("☰")
                        }
                    }
                )
            }
        ) { inner ->
            NavHost(
                navController = navController,
                startDestination = Dest.Notifications.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
            ) {
                // ✅ Real screen for Notifications
                composable(Dest.Notifications.route) { NotificationsScreen() }

                // Placeholders (unchanged behavior)
                composable(Dest.Modules.route)       { PlaceholderScreen("Modules") }
                composable(Dest.Scheduler.route)     { PlaceholderScreen("Scheduler") }
                composable(Dest.Repository.route)    { PlaceholderScreen("Repository") }

                // ✅ Real FAQ screen
                composable(Dest.Faq.route)           { FaqScreen() }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Surface(Modifier.fillMaxSize()) {
        BoxedText(title)
    }
}

@Composable
private fun BoxedText(text: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) { Text(text, style = MaterialTheme.typography.titleMedium) }
}

private fun currentTitle(route: String?): String = when (route) {
    Dest.Notifications.route -> "Notifications"
    Dest.Modules.route       -> "Modules"
    Dest.Scheduler.route     -> "Scheduler"
    Dest.Repository.route    -> "Repository"
    Dest.Faq.route           -> "FAQ"
    else                     -> "CSI Portal"
}
