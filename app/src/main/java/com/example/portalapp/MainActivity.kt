package com.example.portalapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import com.example.portalapp.storage.UserPrefs
import com.example.portalapp.ui.theme.PortalAppTheme
import com.example.portalapp.viewmodels.AuthViewModel
import com.example.portalapp.views.auth.LoginScreen
import com.example.portalapp.views.landing.LandingScreen   // ⬅️ NEW import
import com.example.portalapp.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: UserPrefs

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PortalAppTheme {
                val token by prefs.tokenFlow().collectAsState(initial = null)

                // 🔹 Track if "Get Started" was pressed
                var started by remember { mutableStateOf(false) }

                when {
                    !started -> {
                        LandingScreen(onGetStarted = { started = true })
                    }
                    token.isNullOrEmpty() -> {
                        LoginScreen(onLoggedIn = { /* tokenFlow drives the swap */ })
                    }
                    else -> {
                        val vm: AuthViewModel = hiltViewModel()
                        Navigation(
                            onLogout = { vm.logout { started = false } }
                        )
                    }
                }
            }
        }
    }
}
