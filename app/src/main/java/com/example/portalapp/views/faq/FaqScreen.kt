package com.example.portalapp.views.faq

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.models.FaqEntry
import com.example.portalapp.viewmodels.FaqViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

// ---------- Tweakables / Style surface for easy adjustments ----------
private object FaqStyle {
    // Card container
    val cardContainerColor = Color.White
    val cardAlpha = 0.95f

    // Text scales (relative to theme) to make cards "slimmer" like notifications
    val titleTextScale = 1.1f
    val tapTextScale = 0.80f

    // Padding to slim the cards
    val horizontalPadding = 16.dp
    val collapsedVerticalPadding = 10.dp
    val expandedVerticalPadding = 12.dp
    val betweenTitleAndTapCollapsed = 4.dp
    val betweenTitleAndBodyExpanded = 6.dp
    val betweenBodyAndDateExpanded = 8.dp

    // Tap-to-view hint colour (adjustable)
    val tapHintColor = Color(0xFF9CA3AF)

    // Icon-in-circle (collapsed state)
    val showIconWhenCollapsed = true
    val iconCircleBgOffWhite = Color.White   // off-white background
    val iconCircleBorderWhite = Color.Black        // pure white border
    val iconTintBlack = Color.Black                // black question mark
    val iconCircleAlpha = 1f
    val iconCircleSize = 25.dp
    val iconSize = 24.dp
}
// --------------------------------------------------------------------

private val BLUE = Color(0xFF0D6EFD)

@Composable
fun FaqScreen(
    vm: FaqViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Second bar with search (fixed 40dp height and white background) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.White)
                .padding(start = 12.dp, end = 12.dp, top = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Search field (centered)
                SearchFieldCompact(
                    value = state.search,
                    onValueChange = vm::onSearchChange,
                    onSearch = vm::applySearch,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(34.dp)
                )

                // Blue circular search icon (right), slightly overlapping the field
                val focus = LocalFocusManager.current
                FilledIconButton(
                    onClick = {
                        vm.applySearch()
                        focus.clearFocus()
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .offset(x = (-16).dp)
                        .align(Alignment.CenterVertically),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = BLUE)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        when {
            state.loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            state.error != null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) { Text(state.error!!, color = MaterialTheme.colorScheme.error) }
            }
            state.items.isEmpty() -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (state.search.isBlank()) "No FAQs yet."
                        else "No results for “${state.search}”.",
                        color = LocalContentColor.current.copy(alpha = 0.8f)
                    )
                }
            }
            else -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) { FaqList(state.items) }
            }
        }
    }
}

@Composable
private fun SearchFieldCompact(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .shadow(2.dp, shape = shape, clip = false)
            .border(width = 1.dp, color = Color.White, shape = shape)
            .background(color = Color.White, shape = shape)
            .padding(horizontal = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
        ) {
            Box(Modifier.fillMaxWidth()) {
                if (value.isEmpty()) {
                    Text(
                        text = "search using key words",
                        color = LocalContentColor.current.copy(alpha = 0.6f),
                        style = TextStyle(fontSize = 12.sp, lineHeight = 14.sp),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(vertical = 2.dp)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        color = LocalContentColor.current
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearch()
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun FaqList(items: List<FaqEntry>) {
    var expandedIds by remember { mutableStateOf(setOf<Int>()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(items, key = { it.id }) { entry ->
            val expanded = remember(expandedIds) { expandedIds.contains(entry.id) }
            FaqCard(
                entry = entry,
                expanded = expanded,
                onToggle = {
                    expandedIds =
                        if (expanded) expandedIds - entry.id else expandedIds + entry.id
                }
            )
        }
    }
}

private fun onlyDate(isoString: String): String {
    val out = DateTimeFormatter.ISO_LOCAL_DATE
    return try {
        OffsetDateTime.parse(isoString).toLocalDate().format(out)
    } catch (_: Throwable) {
        try {
            LocalDateTime.parse(isoString).toLocalDate().format(out)
        } catch (_: Throwable) {
            try {
                LocalDate.parse(isoString).format(out)
            } catch (_: Throwable) {
                if (isoString.length >= 10) isoString.substring(0, 10) else isoString
            }
        }
    }
}

@Composable
private fun FaqCard(
    entry: FaqEntry,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .animateContentSize(),
        shape = RectangleShape, // straight edges (no rounding)
        colors = CardDefaults.elevatedCardColors(
            containerColor = FaqStyle.cardContainerColor.copy(alpha = FaqStyle.cardAlpha)
        )
        // elevation left as default, matching notification card feel
    ) {
        val verticalPad = if (expanded) FaqStyle.expandedVerticalPadding else FaqStyle.collapsedVerticalPadding
        Column(
            Modifier.padding(
                horizontal = FaqStyle.horizontalPadding,
                vertical = verticalPad
            )
        ) {
            // Collapsed header: optional icon + title
            if (!expanded) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (FaqStyle.showIconWhenCollapsed) {
                        Box(
                            modifier = Modifier
                                .size(FaqStyle.iconCircleSize)
                                .background(
                                    FaqStyle.iconCircleBgOffWhite.copy(alpha = FaqStyle.iconCircleAlpha),
                                    CircleShape
                                )
                                .border(1.dp, FaqStyle.iconCircleBorderWhite, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Help, // question mark style icon
                                contentDescription = null,
                                tint = FaqStyle.iconTintBlack,
                                modifier = Modifier.size(FaqStyle.iconSize)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    val baseHeader = MaterialTheme.typography.labelLarge
                    Text(
                        text = entry.question,
                        style = baseHeader.copy(fontSize = baseHeader.fontSize * FaqStyle.titleTextScale),
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                }

                Spacer(Modifier.height(FaqStyle.betweenTitleAndTapCollapsed))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val baseTap = MaterialTheme.typography.labelMedium
                    Text(
                        text = "Tap to view answer",
                        style = baseTap.copy(fontSize = baseTap.fontSize * FaqStyle.tapTextScale),
                        color = FaqStyle.tapHintColor
                    )
                }
            } else {
                // Expanded content (kept visually the same per your instruction)
                Text(
                    text = entry.answer,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (entry.lastUpdated.isNotBlank()) {
                    Spacer(Modifier.height(FaqStyle.betweenBodyAndDateExpanded))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = onlyDate(entry.lastUpdated),
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalContentColor.current.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
