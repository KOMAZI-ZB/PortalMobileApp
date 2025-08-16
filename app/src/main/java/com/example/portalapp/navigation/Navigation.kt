package com.example.portalapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.portalapp.views.notifications.NotificationsScreen
import com.example.portalapp.views.faq.FaqScreen
import com.example.portalapp.views.modules.ModulesScreen
import com.example.portalapp.views.modules.ModuleDocumentsScreen
import com.example.portalapp.views.scheduler.SchedulerScreen
import com.example.portalapp.views.repository.RepositoryScreen   // ⬅️ NEW import
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

// ----- Routes -----
private sealed class Dest(val route: String, val label: String) {
    data object Notifications : Dest("notifications", "Notifications")
    data object Modules       : Dest("modules",       "Modules")
    data object Scheduler     : Dest("scheduler",     "Scheduler")
    data object Repository    : Dest("repository",    "Repository")
    data object Faq           : Dest("faq",           "FAQ")
}

// dynamic route for module docs (not in the drawer)
private const val ROUTE_MODULE_DOCS =
    "module/{moduleId}/docs?code={code}&name={name}"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
    val isModuleDocs = currentRoute?.startsWith("module/") == true

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
                        if (isModuleDocs) {
                            // ⬅️ Back arrow on Module Documents screen
                            IconButton(onClick = { navController.popBackStack() }) {
                                Text("←")
                            }
                        } else {
                            // ☰ Drawer toggle elsewhere
                            IconButton(onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }) { Text("☰") }
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
                composable(Dest.Notifications.route) { NotificationsScreen() }

                // pass a lambda so tapping a module card opens its documents
                composable(Dest.Modules.route) {
                    ModulesScreen(
                        onOpenModule = { module ->
                            val code = java.net.URLEncoder.encode(module.code, "utf-8")
                            val name = java.net.URLEncoder.encode(module.name, "utf-8")
                            navController.navigate("module/${module.id}/docs?code=$code&name=$name")
                        }
                    )
                }

                composable(Dest.Scheduler.route)  { SchedulerScreen() }
                composable(Dest.Repository.route) { RepositoryScreen() }  // ⬅️ REAL SCREEN
                composable(Dest.Faq.route)        { FaqScreen() }

                // Module → Documents screen
                composable(
                    route = ROUTE_MODULE_DOCS,
                    arguments = listOf(
                        navArgument("moduleId") { type = NavType.IntType },
                        navArgument("code") { type = NavType.StringType; defaultValue = "" },
                        navArgument("name") { type = NavType.StringType; defaultValue = "" }
                    )
                ) { entry ->
                    val moduleId = entry.arguments!!.getInt("moduleId")
                    val codeEnc = entry.arguments!!.getString("code") ?: ""
                    val nameEnc = entry.arguments!!.getString("name") ?: ""
                    val code = URLDecoder.decode(codeEnc, StandardCharsets.UTF_8)
                    val name = URLDecoder.decode(nameEnc, StandardCharsets.UTF_8)
                    ModuleDocumentsScreen(moduleId = moduleId, moduleTitle = "$code • $name")
                }
            }
        }
    }
}

@Composable private fun PlaceholderScreen(title: String) {
    Surface(Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) { Text(title, style = MaterialTheme.typography.titleMedium) }
    }
}

private fun currentTitle(route: String?): String = when {
    route == Dest.Notifications.route -> "Notifications"
    route == Dest.Modules.route -> "Modules"
    route == Dest.Scheduler.route -> "Scheduler"
    route == Dest.Repository.route -> "Repository"
    route == Dest.Faq.route -> "FAQ"
    route?.startsWith("module/") == true -> "Module Documents"
    else -> "CSI Portal"
}
