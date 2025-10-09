package com.example.portalapp.views.notifications

import android.R
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.portalapp.models.Notification
import com.example.portalapp.viewmodels.NotificationsViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.layout.WindowInsets // for contentWindowInsets

/**
 * Style controls (tweak to taste).
 */
private object NotificationStyle {
    // Screen background (solid colour with adjustable opacity)
    val screenBackgroundColor = Color.White
    val screenBackgroundAlpha = 0f

    // Card container
    val cardContainerColor = Color.White
    val cardAlpha = 0.95f

    // Header colours (collapsed vs expanded)
    val headerCollapsedColor = Color(0xFF000000) // black when not expanded
    val headerExpandedColor = Color(0xFF0D6EFD)  // blue when expanded

    // Body/meta text colours
    val bodyTextColor = Color(0xFF000000)

    // Collapsed → DATE (blue at top-right); Expanded → TIME (green at top-right)
    val metaTimeColor = Color(0xFF16A34A) // time (expanded) → green
    val metaDateColor = Color(0xFF767676) // date (collapsed) → blue

    // Font size controls (relative to theme)
    val headerTextScale = 0.90f   // 1.0 = original size (adjust as needed)
    val metaTextScale = 0.80f    // shrink date/time text (adjust as needed)

    // Divider underline thickness (only when expanded)
    val headerUnderlineStrokeWidthDp = 2.dp

    // --- Icon-in-circle (shown only when collapsed) ---
    // Different circle + icon colours for announcement vs notification
    val iconCircleAnnouncementColor = Color(0xFF4CAF50)
    val iconCircleNotificationColor = Color(0xFFFF9800)
    val iconTintAnnouncementColor = Color.White
    val iconTintNotificationColor = Color.Black
    val iconCircleAlpha = 1f
    val iconCircleSize = 26.dp
    val iconSize = 17.dp

    // Tab bar height (light blue bar) — reduce height here
    val tabBarHeight = 40.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    vm: NotificationsViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(Unit) { vm.refresh() }

    Scaffold(
        topBar = {},
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        // Screen background with adjustable opacity
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    NotificationStyle.screenBackgroundColor.copy(
                        alpha = NotificationStyle.screenBackgroundAlpha
                    )
                )
                .padding(innerPadding)
        ) {
            Column(Modifier.fillMaxSize()) {
                // ── Tabs bar (kept exactly as before, height reduced) ──
                val blue = Color(0xFF0D6EFD)
                val lightBlue = Color(0xFFFFFFFF)

                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(NotificationStyle.tabBarHeight),
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
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        modifier = Modifier.height(NotificationStyle.tabBarHeight),
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "All",
                                fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTab == 0) blue else Color.Black
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        modifier = Modifier.height(NotificationStyle.tabBarHeight),
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Read",
                                fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTab == 1) blue else Color.Black
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        modifier = Modifier.height(NotificationStyle.tabBarHeight),
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "Unread",
                                fontWeight = if (selectedTab == 2) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTab == 2) blue else Color.Black
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
                            NotificationCard(
                                n = n,
                                onMarkRead = { vm.markRead(n.id) }
                            )
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
    var showImage by remember { mutableStateOf(false) }
    // Persist expanded state per-notification
    var expanded by rememberSaveable(n.id) { mutableStateOf(false) }

    // Pre-calc date + time strings
    val formattedDate = remember(n.createdAt) {
        try {
            LocalDate.parse(n.createdAt.take(10))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))
        } catch (_: Exception) {
            n.createdAt.take(10)
        }
    }
    val formattedTime = remember(n.createdAt) { formatTimeAmPm(n.createdAt) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = RectangleShape, // square edges (no rounding)
        colors = CardDefaults.elevatedCardColors(
            containerColor = NotificationStyle.cardContainerColor.copy(alpha = NotificationStyle.cardAlpha)
        )
    ) {
        Column(
            Modifier.padding(horizontal = 16.dp, vertical = if (expanded) 16.dp else 10.dp)
        ) {
            // Header row: [icon if collapsed] TYPE (left) | date (collapsed) OR time (expanded) (right)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Show the icon in a circle only when collapsed
                if (!expanded) {
                    val isAnnouncement = isAnnouncementType(n.type)
                    val icon = if (isAnnouncement) Icons.Filled.Campaign else Icons.Filled.Notifications
                    val circleColor =
                        if (isAnnouncement)
                            NotificationStyle.iconCircleAnnouncementColor
                        else
                            NotificationStyle.iconCircleNotificationColor
                    val tintColor =
                        if (isAnnouncement)
                            NotificationStyle.iconTintAnnouncementColor
                        else
                            NotificationStyle.iconTintNotificationColor

                    Box(
                        modifier = Modifier
                            .size(NotificationStyle.iconCircleSize)
                            .background(
                                circleColor.copy(alpha = NotificationStyle.iconCircleAlpha),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tintColor,
                            modifier = Modifier.size(NotificationStyle.iconSize)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }

                val headerColor =
                    if (expanded) NotificationStyle.headerExpandedColor
                    else NotificationStyle.headerCollapsedColor

                // Only draw underline when expanded
                val headerModifier =
                    if (expanded) {
                        Modifier.drawBehind {
                            val strokeWidth = NotificationStyle.headerUnderlineStrokeWidthDp.toPx()
                            val y = size.height + strokeWidth / 2
                            drawLine(
                                color = headerColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                    } else {
                        Modifier
                    }

                // Header size (adjustable via headerTextScale)
                val baseHeader = MaterialTheme.typography.labelLarge
                Text(
                    text = typeLabel(n.type).uppercase(),
                    style = baseHeader.copy(fontSize = baseHeader.fontSize * NotificationStyle.headerTextScale),
                    fontWeight = FontWeight.Bold,
                    color = headerColor,
                    modifier = headerModifier,
                    maxLines = 1
                )

                Spacer(Modifier.weight(1f))

                // Top-right meta: collapsed → DATE (blue), expanded → TIME (green)
                val metaText = if (expanded) formattedTime else formattedDate
                val metaColor = if (expanded) NotificationStyle.metaTimeColor else NotificationStyle.metaDateColor
                val baseMeta = MaterialTheme.typography.labelMedium
                Text(
                    metaText,
                    style = baseMeta.copy(fontSize = baseMeta.fontSize * NotificationStyle.metaTextScale),
                    color = metaColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(
                    n.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NotificationStyle.bodyTextColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    n.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NotificationStyle.bodyTextColor
                )

                // Optional image (expanded only)
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
                                    .background(Color.Black.copy(alpha = 0.9f))
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

                // Bottom right: Read / Mark as Read (expanded view)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!n.isRead) {
                        TextButton(onClick = onMarkRead) {
                            Text("Mark as read", color = NotificationStyle.bodyTextColor)
                        }
                    } else {
                        Text(
                            "Read",
                            style = MaterialTheme.typography.labelMedium,
                            color = NotificationStyle.bodyTextColor
                        )
                    }
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
    "faqupdate" -> "FAQ Update Notification"
    "general" -> "Announcement"
    else -> type
}

/** Treat "announcement" types for icon selection. */
private fun isAnnouncementType(type: String): Boolean {
    return when (type.lowercase()) {
        "general", "system", "faqupdate" -> true
        else -> false
    }
}

/**
 * Formats a timestamp to "hh:mm AM/PM".
 * Tries common ISO forms (with/without zone) and falls back gracefully.
 */
private fun formatTimeAmPm(ts: String): String {
    val outFmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
    // Try Offset/Zoned/Local patterns in order
    try {
        val odt = OffsetDateTime.parse(ts)
        val z = odt.atZoneSameInstant(ZoneId.systemDefault())
        return z.format(outFmt).uppercase(Locale.ENGLISH)
    } catch (_: Exception) {}

    try {
        val zdt = ZonedDateTime.parse(ts)
        val z = zdt.withZoneSameInstant(ZoneId.systemDefault())
        return z.format(outFmt).uppercase(Locale.ENGLISH)
    } catch (_: Exception) {}

    try {
        val ldt = LocalDateTime.parse(ts)
        val z = ldt.atZone(ZoneId.systemDefault())
        return z.format(outFmt).uppercase(Locale.ENGLISH)
    } catch (_: Exception) {}

    // Fallback: extract "HH:mm" and convert to 12h with leading zero
    val candidate = ts.substringAfter('T', missingDelimiterValue = ts)
    val hhmm = candidate.takeLastWhile { it != ' ' }.take(5)
    if (hhmm.matches(Regex("""\d{2}:\d{2}"""))) {
        val parts = hhmm.split(":")
        val h24 = parts.getOrNull(0)?.toIntOrNull() ?: return hhmm
        val m = parts.getOrNull(1)?.toIntOrNull() ?: return hhmm
        val isAm = h24 < 12
        val h12 = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        return String.format(
            Locale.ENGLISH,
            "%02d:%02d %s",
            h12, m, if (isAm) "AM" else "PM"
        )
    }
    return ts // last resort
}
