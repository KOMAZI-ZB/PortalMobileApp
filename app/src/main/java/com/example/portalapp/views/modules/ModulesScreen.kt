package com.example.portalapp.views.modules

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.models.ClassSession
import com.example.portalapp.models.Module
import com.example.portalapp.viewmodels.ModulesViewModel

// 🔧 TWEAK HERE: icon size & card elevation/color
private val ICON_SIZE = 100.dp               // ⬅️ make the book image bigger/smaller
private val CARD_ELEVATION = 12.dp           // ⬅️ shadow strength
private val CARD_BG_COLOR = Color.White     // ⬅️ card background (pure white)

@Composable
fun ModulesScreen(
    onOpenModule: (Module) -> Unit,
    vm: ModulesViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    Column(Modifier.fillMaxSize()) {
        SemesterTabs(selected = state.semester, onSelect = { vm.setSemester(it) })
        Spacer(Modifier.height(16.dp))

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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(state.items, key = { _, m -> m.id }) { index, m ->
                        ModuleCard(
                            index = index,
                            module = m,
                            onClick = { onOpenModule(m) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SemesterTabs(selected: Int, onSelect: (Int) -> Unit) {
    val index = if (selected == 2) 1 else 0
    val blue = Color(0xFF0D6EFD)
    val lightBlue = Color(0xFFCAF5F6)
    val grey = Color(0xFF6B7280)

    TabRow(
        selectedTabIndex = index,
        modifier = Modifier.fillMaxWidth(),
        containerColor = lightBlue,
        contentColor = blue,
        indicator = { positions ->
            TabRowDefaults.Indicator(
                modifier = Modifier
                    .tabIndicatorOffset(positions[index])
                    .height(2.dp),
                color = blue
            )
        },
        divider = {}
    ) {
        Tab(
            selected = index == 0,
            onClick = { onSelect(1) },
            text = {
                Text(
                    "Semester 1",
                    fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (index == 0) blue else grey
                )
            }
        )
        Tab(
            selected = index == 1,
            onClick = { onSelect(2) },
            text = {
                Text(
                    "Semester 2",
                    fontWeight = if (index == 1) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (index == 1) blue else grey
                )
            }
        )
    }
}

/** White card + bigger book icon */
@Composable
private fun ModuleCard(
    index: Int,
    module: Module,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val number = (index % 8) + 1
    val iconResId by remember(number) {
        mutableIntStateOf(
            context.resources.getIdentifier("book$number", "drawable", context.packageName)
                .takeIf { it != 0 }
                ?: android.R.drawable.ic_menu_agenda
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = CARD_ELEVATION),
        colors = CardDefaults.elevatedCardColors(containerColor = CARD_BG_COLOR) // ⬅️ WHITE
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "Module icon",
                modifier = Modifier.size(ICON_SIZE) // ⬅️ BIGGER ICON
            )

            Text(
                text = module.code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SessionRow(s: ClassSession) {
    Text(
        "• ${s.weekDay} ${s.startTime}–${s.endTime} • ${s.venue}",
        style = MaterialTheme.typography.bodyMedium
    )
}
