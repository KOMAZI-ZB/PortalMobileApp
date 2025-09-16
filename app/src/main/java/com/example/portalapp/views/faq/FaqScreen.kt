package com.example.portalapp.views.faq

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.R
import com.example.portalapp.models.FaqEntry
import com.example.portalapp.viewmodels.FaqViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun FaqScreen(
    vm: FaqViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Search
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SearchFieldCompact(
                value = state.search,
                onValueChange = vm::onSearchChange,
                onSearch = vm::applySearch,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .heightIn(min = 20.dp, max = 30.dp) // thin height per your setting
            )
            TextButton(
                onClick = vm::applySearch,
                enabled = !state.loading
            ) {
                Text("Search", color = Color.Black) // ← Search text in black
            }
        }

        Spacer(Modifier.height(12.dp))

        when {
            state.loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            state.items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (state.search.isBlank()) "No FAQs yet."
                        else "No results for “${state.search}”.",
                        color = LocalContentColor.current.copy(alpha = 0.8f)
                    )
                }
            }
            else -> {
                FaqList(state.items)
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
    val shape = RoundedCornerShape(12.dp)
    val borderColor = Color.Black // ← Border set to black

    // Slim, rounded "outlined" container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = borderColor, shape = shape)
            .background(color = Color.Transparent, shape = shape)
            .padding(horizontal = 2.dp) // compact inner padding
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 30.dp, max = 40.dp) // keep container thin
        ) {
            // Left search icon from res/drawable/search
            Image(
                painter = painterResource(id = R.drawable.search),
                contentDescription = "Search",
                modifier = Modifier
                    .size(50.dp)
            )

            Spacer(Modifier.width(2.dp))

            // Text input area
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 12.sp,          // small so it won't clip at 20–30dp height
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
                        .padding(vertical = 2.dp) // tiny vertical padding
                )

                // Placeholder when empty (sits next to icon)
                if (value.isEmpty()) {
                    Text(
                        text = "search using key words",
                        color = LocalContentColor.current.copy(alpha = 0.6f),
                        style = TextStyle(fontSize = 12.sp, lineHeight = 14.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FaqList(items: List<FaqEntry>) {
    // Track expanded items locally; we don't need to persist on process death.
    var expandedIds by remember { mutableStateOf(setOf<Int>()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp) // proper spacing between/around cards
    ) {
        items(items, key = { it.id }) { entry ->
            val expanded = remember(expandedIds) { expandedIds.contains(entry.id) }
            FaqCard(
                entry = entry,
                expanded = expanded,
                onToggle = {
                    expandedIds = if (expanded) expandedIds - entry.id else expandedIds + entry.id
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
                // Fallback: first 10 chars if already like "YYYY-MM-DD..."
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
            .clickable { onToggle() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = entry.question,
                style = MaterialTheme.typography.titleMedium
            )
            if (expanded) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = entry.answer,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (entry.lastUpdated.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    // Bottom-right, date-only (YYYY-MM-DD), nothing else
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
            } else {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tap to view answer",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
