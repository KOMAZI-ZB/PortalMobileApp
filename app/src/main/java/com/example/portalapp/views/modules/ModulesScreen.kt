package com.example.portalapp.views.modules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.models.ClassSession
import com.example.portalapp.models.Module
import com.example.portalapp.viewmodels.ModulesViewModel

@Composable
fun ModulesScreen(
    onOpenModule: (Module) -> Unit,
    vm: ModulesViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    Column(Modifier.fillMaxSize()) {
        SemesterTabs(selected = state.semester, onSelect = { vm.setSemester(it) })

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
            state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No modules yet.")
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.items, key = { it.id }) { m ->
                        ModuleCard(m, onClick = { onOpenModule(m) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SemesterTabs(selected: Int, onSelect: (Int) -> Unit) {
    val index = if (selected == 2) 1 else 0
    TabRow(
        selectedTabIndex = index,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        indicator = { positions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(positions[index]),
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        Tab(selected = index == 0, onClick = { onSelect(1) }, text = { Text("Semester 1") })
        Tab(selected = index == 1, onClick = { onSelect(2) }, text = { Text("Semester 2") })
    }
}

@Composable
private fun ModuleCard(m: Module, onClick: () -> Unit) {
    // 🔹 Show ONLY the module code (as requested)
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = m.code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SessionRow(s: ClassSession) {
    Text("• ${s.weekDay} ${s.startTime}–${s.endTime} • ${s.venue}", style = MaterialTheme.typography.bodyMedium)
}
