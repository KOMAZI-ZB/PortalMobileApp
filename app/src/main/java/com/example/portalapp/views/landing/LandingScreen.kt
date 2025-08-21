package com.example.portalapp.views.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.portalapp.R

@Composable
fun LandingScreen(
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 🔹 Background image fills screen
        Image(
            painter = painterResource(id = R.drawable.logoimg2),
            contentDescription = "Landing background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop   // ensures it fills while keeping aspect ratio
        )

        // 🔹 Button aligned at the bottom
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .align(Alignment.BottomCenter)  // place at bottom center
                .padding(bottom = 48.dp)
                .fillMaxWidth(0.7f)
                .height(52.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color.Black,
                contentColor = androidx.compose.ui.graphics.Color.White
            )
        ) {
            Text("Get Started", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
