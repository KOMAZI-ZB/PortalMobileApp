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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.R
import com.example.portalapp.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    vm: AuthViewModel = hiltViewModel(),
    // 🔧 Layout tweaks you can adjust from the call site
    iconOffsetX: Dp = 0.dp,
    iconOffsetY: Dp = -160.dp,
    iconSize: Dp = 300.dp,
    contentBottomPadding: Dp = 36.dp,
    contentOffsetY: Dp = 12.dp,
    loginButtonOffsetY: Dp = 8.dp,

    // 🔧 New: controls for icons inside the text fields
    userIconSize: Dp = 50.dp,
    userIconOffsetX: Dp = 0.dp,
    userIconOffsetY: Dp = 0.dp,

    lockIconSize: Dp = 50.dp,
    lockIconOffsetX: Dp = 0.dp,
    lockIconOffsetY: Dp = 0.dp,

    eyeIconSize: Dp = 50.dp,
    eyeIconOffsetX: Dp = 0.dp,
    eyeIconOffsetY: Dp = 0.dp,

    // Optional tint override for all field icons (kept as original colors by default)
    fieldIconTint: Color? = null
) {
    val state by vm.ui.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // ✅ Background image (current code uses background3)
        Image(
            painter = painterResource(id = R.drawable.background5),
            contentDescription = "Login background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ✅ Welcome icon on top of background (adjustable)
        Image(
            painter = painterResource(id = R.drawable.login2),
            contentDescription = "Welcome icon",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = iconOffsetX, y = iconOffsetY)
                .size(iconSize),
            contentScale = ContentScale.Fit
        )

        // ✅ Foreground content (subtitle + fields + actions)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding()
                .padding(bottom = contentBottomPadding)
                .align(Alignment.BottomCenter)
                .offset(y = contentOffsetY),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ⛔️ "Login" heading removed as requested

            // Subtitle
            Text(
                text = "Login with your usernumber and password",
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Username input with adjustable PNG icon
            OutlinedTextField(
                value = state.userName,
                onValueChange = vm::onUserChanged,
                label = { Text("Usernumber") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "User Icon",
                        modifier = Modifier
                            .offset(x = userIconOffsetX, y = userIconOffsetY)
                            .size(userIconSize),
                        tint = fieldIconTint ?: Color.Unspecified
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Password input with adjustable PNG icons
            OutlinedTextField(
                value = state.password,
                onValueChange = vm::onPassChanged,
                label = { Text("Password") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.lock),
                        contentDescription = "Lock Icon",
                        modifier = Modifier
                            .offset(x = lockIconOffsetX, y = lockIconOffsetY)
                            .size(lockIconSize),
                        tint = fieldIconTint ?: Color.Unspecified
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.offset(x = eyeIconOffsetX, y = eyeIconOffsetY)
                    ) {
                        Icon(
                            painter = painterResource(
                                if (passwordVisible) R.drawable.visibilityoff else R.drawable.visibilityon
                            ),
                            contentDescription = "Toggle Password",
                            modifier = Modifier.size(eyeIconSize),
                            tint = fieldIconTint ?: Color.Unspecified
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )

            // ✅ Remember me moved to the RIGHT; Forgot Password removed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text("Remember me", color = Color.Black, fontSize = 14.sp)
                }
            }

            // Error text
            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            // ✅ Black login button, slightly larger font; positioned a bit lower
            Button(
                onClick = { vm.login(onLoggedIn) },
                enabled = !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .offset(y = loginButtonOffsetY),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    if (state.loading) "Logging in…" else "Login",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
