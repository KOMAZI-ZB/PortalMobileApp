package com.example.portalapp.views.modules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.models.Module
import com.example.portalapp.viewmodels.ModulesViewModel
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen(
    onOpenModule: (Module) -> Unit,
    vm: ModulesViewModel = hiltViewModel(),

    // ───────── Adjustable styling knobs (kept, plus new size knobs) ─────────
    tabBarHeight: Dp = 40.dp,
    tabBarBackgroundColor: Color = Color.White,
    semesterTabTextColor: Color = Color.Black,                  // unselected = black
    selectedTabTextColor: Color = Color(0xFF0D6EFD),            // selected = blue
    indicatorColor: Color = Color(0xFF0D6EFD),                  // blue underline

    cardCornerRadius: Dp = 0.dp,                                // 0 = sharp corners
    cardBorderColor: Color = Color.White,
    cardBorderThickness: Dp = 2.dp,
    cardShadowOpacity: Float = 0.25f,                           // adjustable shadow opacity
    cardShadowElevation: Dp = 8.dp,                             // adjustable elevation

    // NEW: background & card opacity controls
    screenBackgroundColor: Color = Color.White,
    screenBackgroundAlpha: Float = 0f,                          // 0 = transparent (default)
    cardContainerColor: Color = Color.White,
    cardAlpha: Float = 0.95f,                                      // 1 = fully opaque (default)

    // NEW: make the cards thinner & tweakable
    cardMinHeight: Dp = 60.dp,                                  // ↓ was 140.dp
    cardInnerPadding: Dp = 4.dp,                                // ↓ tighter inner padding (both columns)

    moduleCodeTextStyle: TextStyle = MaterialTheme.typography.titleMedium .copy(
        fontWeight = FontWeight.Bold
    ),                                                          // header-like
    moduleNameTextStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Light
    )                                                           // slimmer / lighter
) {
    val ui by vm.ui.collectAsState()
    val blue = Color(0xFF0D6EFD)

    // Keep semester (1/2) in sync with VM
    var selectedTab by remember { mutableStateOf(if (ui.semester == 2) 1 else 0) }
    LaunchedEffect(selectedTab) {
        val desiredSemester = if (selectedTab == 0) 1 else 2
        if (desiredSemester != ui.semester) vm.setSemester(desiredSemester)
    }

    // Choose shape from corner radius (0 → square)
    val cardShape: Shape = if (cardCornerRadius.value <= 0f) RectangleShape else RoundedCornerShape(cardCornerRadius)

    Column(
        Modifier
            .fillMaxSize()
            // ⬇️ Screen background with adjustable opacity
            .background(screenBackgroundColor.copy(alpha = screenBackgroundAlpha))
    ) {
        // ─────────────────── 2nd (top) tab bar: white, 40dp ───────────────────
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .height(tabBarHeight)
                .background(tabBarBackgroundColor),
            containerColor = tabBarBackgroundColor,
            contentColor = blue,
            indicator = { positions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier
                        .tabIndicatorOffset(positions[selectedTab])
                        .height(2.dp),
                    color = indicatorColor
                )
            },
            divider = {} // no divider line
        ) {
            listOf("Semester 1", "Semester 2").forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                Tab(
                    selected = isSelected,
                    modifier = Modifier.height(tabBarHeight),
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            label,
                            color = if (isSelected) selectedTabTextColor else semesterTabTextColor,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // ─────────────────────── Content area ───────────────────────
        when {
            ui.loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            ui.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(ui.error ?: "Error", color = MaterialTheme.colorScheme.error)
                }
            }
            ui.items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No modules found.")
                }
            }
            else -> {
                // Grid untouched; only card thickness changed
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    content = {
                        items(ui.items, key = { it.id }) { m ->
                            ModuleCard(
                                module = m,
                                onClick = { onOpenModule(m) },
                                cardShape = cardShape,
                                cardBorderColor = cardBorderColor,
                                cardBorderThickness = cardBorderThickness,
                                cardShadowElevation = cardShadowElevation,
                                cardShadowOpacity = cardShadowOpacity,
                                cardContainerColor = cardContainerColor,    // NEW
                                cardAlpha = cardAlpha,                      // NEW
                                cardMinHeight = cardMinHeight,
                                cardInnerPadding = cardInnerPadding,
                                moduleCodeTextStyle = moduleCodeTextStyle,
                                moduleNameTextStyle = moduleNameTextStyle
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ModuleCard(
    module: Module,
    onClick: () -> Unit,
    cardShape: Shape,
    cardBorderColor: Color,
    cardBorderThickness: Dp,
    cardShadowElevation: Dp,
    cardShadowOpacity: Float,
    cardContainerColor: Color,
    cardAlpha: Float,
    cardMinHeight: Dp,
    cardInnerPadding: Dp,
    moduleCodeTextStyle: TextStyle,
    moduleNameTextStyle: TextStyle
) {
    val lightBlue = Color(0xFFCAF5F6) // same blue as scheduler/assessment cards

    // Shadow + bordered surface (kept)
    Box(
        modifier = Modifier
            .shadow(
                elevation = cardShadowElevation,
                shape = cardShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = cardShadowOpacity),
                spotColor = Color.Black.copy(alpha = cardShadowOpacity)
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = cardMinHeight) // thinner, adjustable
                .clickable(onClick = onClick),
            shape = cardShape, // square if radius = 0
            color = cardContainerColor.copy(alpha = cardAlpha),   // ⬅️ adjustable card opacity
            border = BorderStroke(cardBorderThickness, cardBorderColor)
        ) {
            // Two-column layout (unchanged concept)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // LEFT: module code (white side, centered)
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight()
                        .padding(cardInnerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = module.code,
                        style = moduleCodeTextStyle,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // RIGHT: module name (light blue side) → centered vertically, LEFT-aligned
                Column(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                        .background(color = lightBlue)
                        .padding(cardInnerPadding),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = module.name,
                        style = moduleNameTextStyle,
                        maxLines = 2,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
