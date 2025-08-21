package com.example.portalapp.views.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.R
import com.example.portalapp.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // ✅ Background image
        Image(
            painter = painterResource(id = R.drawable.loginimg),
            contentDescription = "Login background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ✅ Foreground content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 80.dp) // ⬅ adjust height
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main title
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            // Subtitle
            Text(
                text = "Login with your usernumber and password",
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username input with PNG icon
            OutlinedTextField(
                value = state.userName,
                onValueChange = vm::onUserChanged,
                label = { Text("Usernumber") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.user), // user.png in drawable
                        contentDescription = "User Icon",
                        modifier = Modifier.size(30.dp), // ⬅ bigger
                        tint = Color.Unspecified
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Password input with PNG icons
            OutlinedTextField(
                value = state.password,
                onValueChange = vm::onPassChanged,
                label = { Text("Password") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.lock), // lock.png in drawable
                        contentDescription = "Lock Icon",
                        modifier = Modifier.size(30.dp), // ⬅ bigger
                        tint = Color.Unspecified
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(
                                if (passwordVisible) R.drawable.visibilityoff else R.drawable.visibilityon
                            ), // PNG eye icons
                            contentDescription = "Toggle Password",
                            modifier = Modifier.size(30.dp), // ⬅ bigger
                            tint = Color.Unspecified
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )

            // Remember me + Forgot password row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text("Remember me", color = Color.Black, fontSize = 14.sp)
                }
                TextButton(onClick = { /* TODO: handle forgot password */ }) {
                    Text("Forgot Password?", color = Color.Black, fontSize = 14.sp)
                }
            }

            // Error text
            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            // ✅ Black login button with white text
            Button(
                onClick = { vm.login(onLoggedIn) },
                enabled = !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(if (state.loading) "Logging in…" else "Login")
            }
        }
    }
}
