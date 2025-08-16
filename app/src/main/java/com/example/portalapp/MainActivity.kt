package com.example.portalapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.portalapp.storage.UserPrefs
import com.example.portalapp.ui.theme.PortalAppTheme
import com.example.portalapp.viewmodels.AuthViewModel
import com.example.portalapp.views.auth.LoginScreen
import com.example.portalapp.navigation.Navigation       // ← new navigation entry point
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PortalAppTheme {
                // Observe token; UI swaps between Login and Home automatically
                val token by prefs.tokenFlow().collectAsState(initial = null)

                if (token.isNullOrEmpty()) {
                    LoginScreen(onLoggedIn = { /* tokenFlow drives the swap */ })
                } else {
                    val vm: AuthViewModel = hiltViewModel()
                    Navigation(
                        onLogout = { vm.logout { /* token cleared; shows LoginScreen */ } }
                    )
                }
            }
        }
    }
}
