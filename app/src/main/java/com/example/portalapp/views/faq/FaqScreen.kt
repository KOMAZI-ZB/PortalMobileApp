package com.example.portalapp.views.faq

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.models.FaqEntry
import com.example.portalapp.viewmodels.FaqViewModel

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
            OutlinedTextField(
                value = state.search,
                onValueChange = vm::onSearchChange,
                label = { Text("Search FAQs") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { vm.applySearch() }
                )
            )
            TextButton(
                onClick = vm::applySearch,
                enabled = !state.loading
            ) {
                Text("Search")
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
private fun FaqList(items: List<FaqEntry>) {
    // Track expanded items locally; we don't need to persist on process death.
    var expandedIds by remember { mutableStateOf(setOf<Int>()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 12.dp)
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
                    Text(
                        text = "Last updated: ${entry.lastUpdated}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalContentColor.current.copy(alpha = 0.7f)
                    )
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
