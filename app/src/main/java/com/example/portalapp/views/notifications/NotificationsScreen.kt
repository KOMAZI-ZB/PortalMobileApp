package com.example.portalapp.views.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.models.Notification
import com.example.portalapp.viewmodels.NotificationsViewModel

@Composable
fun NotificationsScreen(
    vm: NotificationsViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    // 🔹 Tab state: 0 = All, 1 = Read, 2 = Unread
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(Unit) { vm.refresh() }

    Box(Modifier.fillMaxSize()) {
        when {
            state.loading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            state.error != null -> {
                Text(
                    state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
            else -> {
                Column(Modifier.fillMaxSize()) {

                    // 🔹 Filter tabs just below the Notifications label (TopAppBar title)
                    TabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        indicator = { positions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(positions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = LocalContentColor.current.copy(alpha = 0.7f),
                            text = { Text("All") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = LocalContentColor.current.copy(alpha = 0.7f),
                            text = { Text("Read") }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = LocalContentColor.current.copy(alpha = 0.7f),
                            text = { Text("Unread") }
                        )
                    }

                    val itemsToShow = remember(state.items, selectedTab) {
                        when (selectedTab) {
                            1 -> state.items.filter { it.isRead }     // Read
                            2 -> state.items.filter { !it.isRead }    // Unread
                            else -> state.items                        // All
                        }
                    }

                    if (itemsToShow.isEmpty()) {
                        // Keep messaging simple; respects current filter
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (state.items.isEmpty()) "No notifications yet."
                                else "No items for this filter.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(itemsToShow, key = { it.id }) { n ->
                                NotificationCard(
                                    n,
                                    onMarkRead = { vm.markRead(n.id) }
                                )
                            }
                        }
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
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Small type "chip" with exact website wording
                AssistChip(
                    onClick = {},
                    label = { Text(typeLabel(n.type)) },
                    enabled = false
                )
                if (!n.isRead) {
                    Text("• Unread", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    n.createdAt.take(19).replace('T', ' '),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                n.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(n.message, style = MaterialTheme.typography.bodyMedium)

            if (!n.isRead) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onMarkRead) { Text("Mark as read") }
            }
        }
    }
}

// 🔹 Exact labels to match website phrasing
private fun typeLabel(type: String): String = when (type.lowercase()) {
    "system" -> "System Announcement"
    "documentupload" -> "Document Upload Notification"
    "repositoryupdate" -> "Repository Update Notification"
    "scheduleupdate", "schedulerupdate" -> "Schedule Update Notification"
    "general" -> "Announcement"
    else -> type
}
