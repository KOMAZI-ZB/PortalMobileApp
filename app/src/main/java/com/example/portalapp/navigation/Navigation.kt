package com.example.portalapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.portalapp.R
import com.example.portalapp.views.faq.FaqScreen
import com.example.portalapp.views.modules.ModuleDocumentsScreen
import com.example.portalapp.views.modules.ModulesScreen
import com.example.portalapp.views.notifications.NotificationsScreen
import com.example.portalapp.views.repository.RepositoryScreen
import com.example.portalapp.views.scheduler.SchedulerScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

// ----- Routes -----
private sealed class Dest(val route: String, val label: String, val iconRes: Int) {
    data object Notifications : Dest("notification", "Notification", R.drawable.notification)
    data object Modules       : Dest("modules",       "Modules",       R.drawable.modules)
    data object Scheduler     : Dest("scheduler",     "Scheduler",     R.drawable.scheduler)
    data object Repository    : Dest("repository",    "Repository",    R.drawable.repository)
    data object Faq           : Dest("faq",           "FAQ",           R.drawable.faq)
}

// Dynamic route for module docs (detail screen; no bottom bar)
private const val ROUTE_MODULE_DOCS =
    "module/{moduleId}/docs?code={code}&name={name}"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(
    onLogout: () -> Unit,
    // 🔧 Bottom bar styling knobs
    bottomBarColor: Color = Color(0xFF0D6EFD),                 // Blue bar
    labelSelectedColor: Color = Color.White,                   // White text (selected)
    labelUnselectedColor: Color = Color.White.copy(alpha = .85f),
    iconSelectedColor: Color = Color.White,                    // White icons (selected)
    iconUnselectedColor: Color = Color.White.copy(alpha = .85f),
    indicatorColor: Color = Color.White.copy(alpha = .12f),    // subtle white overlay for selected tab
    defaultIconSize: Dp = 40.dp,                               // default icon size
    defaultLabelSizeSp: Int = 10,                              // default label size
    // Optional per-tab fine-tuning (override by route)
    iconSizeByRoute: Map<String, Dp> = emptyMap(),
    labelSizeByRouteSp: Map<String, Int> = emptyMap(),
    alwaysShowLabels: Boolean = true,
    // 🔧 Top bar adjustable sizes (NEW)
    topTitleSizeSp: Int = 25,                                  // change title text size here
    logoutIconSize: Dp = 70.dp                                 // change logout icon size here
) {
    val navController = rememberNavController()
    val items = listOf(
        Dest.Notifications,
        Dest.Modules,
        Dest.Scheduler,
        Dest.Repository,
        Dest.Faq
    )

    val blue = Color(0xFF0D6EFD)
    val white = Color.White

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isModuleDocs = currentRoute?.startsWith("module/") == true

    var showLogoutConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentTitle(currentRoute),
                        color = white,
                        fontSize = topTitleSizeSp.sp, // ← adjustable title size
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    if (isModuleDocs) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = white
                            )
                        }
                    }
                },
                actions = {
                    // Logout icon at the RIGHT of the top bar
                    IconButton(onClick = { showLogoutConfirm = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.logout),
                            contentDescription = "Logout",
                            tint = white,
                            modifier = Modifier.size(logoutIconSize) // ← adjustable logout icon size
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = blue,
                    titleContentColor = white,
                    navigationIconContentColor = white,
                    actionIconContentColor = white
                )
            )
        },
        bottomBar = {
            // Show bottom nav only on top-level screens (i.e., when logged in & not on details)
            if (!isModuleDocs) {
                NavigationBar(
                    containerColor = bottomBarColor,
                    tonalElevation = 0.dp
                ) {
                    items.forEach { dest ->
                        val selected = currentRoute == dest.route
                        val iconSize = iconSizeByRoute[dest.route] ?: defaultIconSize
                        val labelSize = (labelSizeByRouteSp[dest.route] ?: defaultLabelSizeSp).sp

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = dest.iconRes),
                                    contentDescription = dest.label,
                                    modifier = Modifier.size(iconSize)
                                )
                            },
                            label = { Text(dest.label, fontSize = labelSize) },
                            alwaysShowLabel = alwaysShowLabels,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = iconSelectedColor,
                                selectedTextColor = labelSelectedColor,
                                unselectedIconColor = iconUnselectedColor,
                                unselectedTextColor = labelUnselectedColor,
                                indicatorColor = indicatorColor
                            )
                        )
                    }
                }
            }
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
            composable(Dest.Repository.route) { RepositoryScreen() }
            composable(Dest.Faq.route)        { FaqScreen() }

            // Module → Documents screen (detail)
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

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                IconButton(onClick = {
                    showLogoutConfirm = false
                    onLogout()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                IconButton(onClick = { showLogoutConfirm = false }) {
                    Text("No")
                }
            }
        )
    }
}

private fun currentTitle(route: String?): String = when {
    route == Dest.Notifications.route -> "Notification"
    route == Dest.Modules.route -> "Modules"
    route == Dest.Scheduler.route -> "Scheduler"
    route == Dest.Repository.route -> "Repository"
    route == Dest.Faq.route -> "FAQ"
    route?.startsWith("module/") == true -> "Module Documents"
    else -> "CSI Portal"
}
