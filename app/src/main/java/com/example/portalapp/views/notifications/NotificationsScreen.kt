package com.example.portalapp.views.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.portalapp.models.Notification
import com.example.portalapp.viewmodels.NotificationsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.WindowInsets // ⬅️ for contentWindowInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    vm: NotificationsViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(Unit) { vm.refresh() }

    // No TopAppBar here -> prevents duplicate header.
    // IMPORTANT: remove default content insets so there's NO gap under the app-level TopAppBar.
    Scaffold(
        topBar = {},
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { /*innerPadding not needed now*/ _ ->
        Column(
            Modifier
                .fillMaxSize()
        ) {
            // ── Tabs bar (light blue background, blue indicator, grey unselected) ──
            val blue = Color(0xFF0D6EFD)
            val lightBlue = Color(0xFFCAF5F6)
            val grey = Color(0xFF6B7280)

            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = lightBlue,
                contentColor = blue,
                indicator = { positions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .tabIndicatorOffset(positions[selectedTab])
                            .height(2.dp),
                        color = blue
                    )
                },
                divider = {} // no extra divider line
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "All",
                            fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == 0) blue else grey
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Read",
                            fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == 1) blue else grey
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Unread",
                            fontWeight = if (selectedTab == 2) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == 2) blue else grey
                        )
                    }
                )
            }

            val itemsToShow = remember(state.items, selectedTab) {
                when (selectedTab) {
                    1 -> state.items.filter { it.isRead }
                    2 -> state.items.filter { !it.isRead }
                    else -> state.items
                }
            }

            if (itemsToShow.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notifications available.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(itemsToShow, key = { it.id }) { n ->
                        NotificationCard(n, onMarkRead = { vm.markRead(n.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    n: Notification,
    onMarkRead: () -> Unit
) {
    var showImage by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFEDF3FF))
    ) {
        Column(Modifier.padding(16.dp)) {

            // Header: type + date
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                val blue = Color(0xFF0D6EFD)
                Text(
                    text = typeLabel(n.type).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = blue,
                    modifier = Modifier.drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        val y = size.height + strokeWidth / 2
                        drawLine(
                            color = blue,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
                )
                Spacer(Modifier.weight(1f))

                val formattedDate = try {
                    LocalDate.parse(n.createdAt.take(10))
                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                } catch (_: Exception) {
                    n.createdAt.take(10)
                }
                Text(formattedDate, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }

            Spacer(Modifier.height(8.dp))
            Text(n.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(n.message, style = MaterialTheme.typography.bodyMedium)

            // Image (click to expand)
            n.imagePath?.let { url ->
                Spacer(Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(url),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clickable { showImage = true },
                    contentScale = ContentScale.Crop
                )

                if (showImage) {
                    Dialog(onDismissRequest = { showImage = false }) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                                .clickable { showImage = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Bottom right: Read / Mark as Read
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!n.isRead) {
                    TextButton(onClick = onMarkRead) { Text("Mark as read", color = Color.Gray) }
                } else {
                    Text("Read", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }
        }
    }
}

private fun typeLabel(type: String): String = when (type.lowercase()) {
    "system" -> "System Announcement"
    "documentupload" -> "Document Upload Notification"
    "repositoryupdate" -> "Repository Update Notification"
    "scheduleupdate", "schedulerupdate" -> "Schedule Update Notification"
    "general" -> "Announcement"
    else -> type
}
