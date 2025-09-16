package com.example.portalapp.views.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.portalapp.R

@Composable
fun LandingScreen(
    onGetStarted: () -> Unit,
    // 🔧 Adjust the landing icon position and size as needed
    iconOffsetX: Dp = 0.dp,
    iconOffsetY: Dp = -40.dp,
    iconSize: Dp = 300.dp,
    // 🔧 Adjust how far from the bottom the button sits
    buttonBottomPadding: Dp = 140.dp
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 🔹 New background image fills the entire screen
        Image(
            painter = painterResource(id = R.drawable.background1),
            contentDescription = "Landing background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🔹 Centered overlay icon (adjustable via offsets & size)
        Image(
            painter = painterResource(id = R.drawable.landingicon),
            contentDescription = "Landing icon",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = iconOffsetX, y = iconOffsetY)
                .size(iconSize),
            contentScale = ContentScale.Fit
        )

        // 🔹 Get Started button (vertical position adjustable)
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = buttonBottomPadding)
                .fillMaxWidth(0.7f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text("Get Started", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
