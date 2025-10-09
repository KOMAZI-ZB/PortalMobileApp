package com.example.portalapp.views.scheduler

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.models.Assessment
import com.example.portalapp.models.ClassScheduleItem
import com.example.portalapp.models.LabBooking
import com.example.portalapp.util.PdfUtils
import com.example.portalapp.viewmodels.SchedulerViewModel
import java.text.SimpleDateFormat
import java.util.Locale

// Week/Date utilities (Lab UI only)
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek
import java.time.YearMonth

// Material vector icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess

/* ------------------ Tweakables you asked for ------------------ */
// Shared: download icon size
private val DOWNLOAD_ICON_SIZE = 60.dp
// Lab: booking card elevation + tiny gap
private val BOOKING_CARD_ELEVATION = 10.dp
private val BOOKING_TEXT_GAP = 2.dp

// Class-specific tweakables
private val CLASS_CARD_ELEVATION = 10.dp
private val MODULE_VENUE_GAP = 8.dp

private val VENUE_ICON_SIZE = 16.dp
private val VENUE_ICON_START_NUDGE = (-4).dp
private val VENUE_TEXT_GAP = 0.dp

// ✨ New font-size knobs
private val MODULE_CODE_FONT_SIZE = 17.sp
private val VENUE_TEXT_FONT_SIZE = 12.sp

private val WEEKDAY_CELL_HEIGHT = 56.dp
private val WEEKDAY_GAP = 6.dp
private val WEEKDAY_CORNER = 16.dp
private val SEMESTER_LABEL_SIZE = 16.sp // (kept for reference; chips removed)
/* -------------------------------------------------------------- */

@Composable
fun SchedulerScreen(
    vm: SchedulerViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current

    var pendingPdfBytes by remember { mutableStateOf<ByteArray?>(null) }
    val createDoc = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val bytes = pendingPdfBytes
        if (uri != null && bytes != null) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            Toast.makeText(context, "Saved.", Toast.LENGTH_SHORT).show()
        }
        pendingPdfBytes = null
    }

    // Colors to match your app
    val blue = Color(0xFF0D6EFD)
    val lightBlue = Color(0xFFCAF5F6)
    val offWhite = Color(0xFFF7F7F7)
    val grey = Color(0xFF6B7280)

    Column(Modifier.fillMaxSize()) {
        // ===== Top bar: main tabs (unchanged) =====
        TabRow(
            selectedTabIndex = state.selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            containerColor = Color.White,
            contentColor = blue,
            indicator = { positions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier
                        .tabIndicatorOffset(positions[state.selectedTab])
                        .height(2.dp),
                    color = blue
                )
            },
            divider = {}
        ) {
            Tab(
                selected = state.selectedTab == 0,
                modifier = Modifier.height(40.dp),
                onClick = { vm.onTabChange(0) },
                text = {
                    Text(
                        "Lab",
                        fontWeight = if (state.selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (state.selectedTab == 0) blue else Color.Black
                    )
                }
            )
            Tab(
                selected = state.selectedTab == 1,
                modifier = Modifier.height(40.dp),
                onClick = { vm.onTabChange(1) },
                text = {
                    Text(
                        "Class",
                        fontWeight = if (state.selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (state.selectedTab == 1) blue else Color.Black
                    )
                }
            )
            Tab(
                selected = state.selectedTab == 2,
                modifier = Modifier.height(40.dp),
                onClick = { vm.onTabChange(2) },
                text = {
                    Text(
                        "Assessments",
                        fontWeight = if (state.selectedTab == 2) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (state.selectedTab == 2) blue else Color.Black
                    )
                }
            )
        }

        // ===== Thin 3rd bar: Semester filter (unchanged) =====
        val semesterTabIndex = if (state.semester == 1) 0 else 1
        TabRow(
            selectedTabIndex = semesterTabIndex,
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            containerColor = offWhite,
            contentColor = blue,
            indicator = { positions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier
                        .tabIndicatorOffset(positions[semesterTabIndex])
                        .height(2.dp),
                    color = blue
                )
            },
            divider = {}
        ) {
            Tab(
                selected = semesterTabIndex == 0,
                modifier = Modifier.height(20.dp),
                onClick = { vm.setSemester(1) },
                text = {
                    Text(
                        "Semester 1",
                        color = if (semesterTabIndex == 0) blue else Color.Black,
                        fontSize = 12.sp,
                        fontWeight = if (semesterTabIndex == 0) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = semesterTabIndex == 1,
                modifier = Modifier.height(20.dp),
                onClick = { vm.setSemester(2) },
                text = {
                    Text(
                        "Semester 2",
                        color = if (semesterTabIndex == 1) blue else Color.Black,
                        fontSize = 12.sp,
                        fontWeight = if (semesterTabIndex == 1) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
        }

        // ===== Content =====
        val labSemesterFiltered = remember(state.lab, state.semester) {
            val monthsAllowed = if (state.semester == 1) 1..6 else 7..12
            val iso = DateTimeFormatter.ISO_LOCAL_DATE
            state.lab.filter { lb ->
                runCatching { LocalDate.parse(lb.bookingDate, iso) }.getOrNull()
                    ?.monthValue in monthsAllowed
            }
        }

        when (state.selectedTab) {
            0 -> LabTab(
                semester = state.semester,
                loading = state.labLoading,
                error = state.labError,
                items = labSemesterFiltered,
                onRetry = vm::refreshLab,
                onDownloadWeek = { weekItems, weekLabel ->
                    val (title, headers, rows) = labPdfData(weekItems)
                    val bytes = PdfUtils.buildSimpleTablePdf("$title • $weekLabel", headers, rows)
                    pendingPdfBytes = bytes
                    val safe = weekLabel.replace(" ", "_").replace("–", "-")
                    createDoc.launch("Lab_Schedule_$safe.pdf")
                }
            )
            1 -> ClassTab(
                loading = state.classLoading,
                error = state.classError,
                items = state.classes,
                onRetry = vm::refreshClass,
                onDownloadAll = {
                    val (title, headers, rows) = classPdfData(state.classes)
                    val bytes = PdfUtils.buildSimpleTablePdf(title, headers, rows)
                    pendingPdfBytes = bytes
                    createDoc.launch("Class_Timetable_Sem${state.semester}.pdf")
                }
            )
            2 -> AssessmentTab(
                loading = state.assessLoading,
                error = state.assessError,
                items = state.assessments,
                onRetry = vm::refreshAssessments,
                onDownloadAll = {
                    val (title, headers, rows) = assessmentPdfData(state.assessments)
                    val bytes = PdfUtils.buildSimpleTablePdf(title, headers, rows)
                    pendingPdfBytes = bytes
                    createDoc.launch("Assessments_Sem${state.semester}.pdf")
                }
            )
        }
    }
}

/* ------------------------- LAB TAB (UPDATED) ------------------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LabTab(
    semester: Int,
    loading: Boolean,
    error: String?,
    items: List<LabBooking>,
    onRetry: () -> Unit,
    onDownloadWeek: (weekItems: List<LabBooking>, weekLabel: String) -> Unit
) {
    val today = LocalDate.now()
    val anchorYear = today.year

    val (minWeekStart, maxWeekStart) = remember(semester) { semesterWeekWindow(anchorYear, semester) }

    val initialWeekStart = remember(today, semester) {
        val thisMonday = if (today.dayOfWeek == DayOfWeek.SUNDAY) today.plusDays(1)
        else today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        clampWeekToWindow(thisMonday, anchorYear, semester)
    }

    var weekStart by remember { mutableStateOf(initialWeekStart) }
    val weekEnd by remember(weekStart) { mutableStateOf(weekStart.plusDays(5)) }

    LaunchedEffect(semester) {
        weekStart = clampWeekToWindow(weekStart, anchorYear, semester)
    }

    var selectedIndex by remember(weekStart) {
        mutableStateOf(
            when {
                today.isBefore(weekStart) || today.isAfter(weekEnd) -> 0
                else -> (today.dayOfWeek.value - 1).coerceIn(0, 5)
            }
        )
    }
    val selectedDay = remember(weekStart, selectedIndex) { weekStart.plusDays(selectedIndex.toLong()) }

    // Month title (July overlap rule kept)
    val monthTitle = remember(weekStart, semester, anchorYear) {
        val locale = Locale.getDefault()
        if (semester == 2) {
            val july1 = LocalDate.of(anchorYear, 7, 1)
            val end = weekStart.plusDays(5)
            if (!weekStart.isAfter(july1) && !end.isBefore(july1)) {
                july1.month.getDisplayName(TextStyle.FULL, locale)
            } else {
                weekStart.month.getDisplayName(TextStyle.FULL, locale)
            }
        } else {
            weekStart.month.getDisplayName(TextStyle.FULL, locale)
        }
    }

    fun weekLabelShort(start: LocalDate): String {
        val end = start.plusDays(5)
        val monShortStart = start.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val monShortEnd = end.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        return if (start.month == end.month && start.year == end.year) {
            "${start.dayOfMonth}–${end.dayOfMonth} $monShortEnd ${end.year}"
        } else {
            "${start.dayOfMonth} $monShortStart – ${end.dayOfMonth} $monShortEnd ${end.year}"
        }
    }
    val weekLabel = remember(weekStart) { weekLabelShort(weekStart) }

    val iso = DateTimeFormatter.ISO_LOCAL_DATE
    val hasWeekBookings by remember(items, weekStart) {
        mutableStateOf(
            items.any {
                runCatching { LocalDate.parse(it.bookingDate, iso) }.getOrNull()
                    ?.let { d -> !d.isBefore(weekStart) && !d.isAfter(weekEnd) } == true
            }
        )
    }

    val weekItems by remember(items, weekStart) {
        mutableStateOf(
            items.filter {
                runCatching { LocalDate.parse(it.bookingDate, iso) }.getOrNull()
                    ?.let { d -> !d.isBefore(weekStart) && !d.isAfter(weekEnd) } == true
            }.sortedWith(compareBy({ it.bookingDate }, { it.startTime }))
        )
    }

    // Day items (for the selected date)
    val selectedKey = selectedDay.format(iso)
    val dayItems = remember(items, selectedDay) {
        items.filter { it.bookingDate == selectedKey }.sortedBy { it.startTime }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            error != null -> ErrorBox(error, onRetry)
            items.isEmpty() -> EmptyBox("No lab bookings.")
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Top part that should collapse away
                    item(key = "month-week-title") {
                        MonthTitle(monthTitle)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { weekStart = clampWeekToWindow(weekStart.minusDays(7), anchorYear, semester) },
                                enabled = weekStart > minWeekStart
                            ) {
                                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous week", tint = Color.Unspecified)
                            }
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "Week: $weekLabel",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = { weekStart = clampWeekToWindow(weekStart.plusDays(7), anchorYear, semester) },
                                enabled = weekStart < maxWeekStart
                            ) {
                                Icon(Icons.Filled.ChevronRight, contentDescription = "Next week", tint = Color.Unspecified)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // Pinned header (weekday picker + "Today/Thursday" line)
                    stickyHeader(key = "lab-sticky") {
                        Surface(color = MaterialTheme.colorScheme.background) {
                            Column(Modifier.fillMaxWidth()) {
                                WeekStripCard(
                                    mondayStart = weekStart,
                                    selectedDay = selectedDay,
                                    onSelect = { date ->
                                        selectedIndex = (date.dayOfWeek.value - 1).coerceIn(0, 5)
                                    }
                                )
                                Spacer(Modifier.height(12.dp))
                                DayHeader(
                                    selectedDay = selectedDay,
                                    today = today,
                                    hasBookings = hasWeekBookings,
                                    onDownload = { onDownloadWeek(weekItems, weekLabel) }
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }

                    // Cards list
                    if (dayItems.isEmpty()) {
                        item(key = "lab-empty-day") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No bookings for this day.", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else {
                        items(dayItems, key = { it.id }) { b ->
                            val displayName = listOfNotNull(
                                b.firstName?.trim().takeUnless { it.isNullOrEmpty() },
                                b.lastName?.trim().takeUnless { it.isNullOrEmpty() }
                            ).joinToString(" ").ifBlank { b.userName }

                            BookingRowTwoColumn(
                                start = b.startTime,
                                end = b.endTime,
                                description = b.description,
                                bookedBy = displayName
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

/* ---- Month title ---- */
@Composable
private fun MonthTitle(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/* ---- Week strip as ONE card split equally ---- */
@Composable
private fun WeekStripCard(
    mondayStart: LocalDate,
    selectedDay: LocalDate,
    onSelect: (LocalDate) -> Unit
) {
    val lightBlue = Color(0xFFCAF5F6)
    val locale = Locale.getDefault()
    val segmentShape = RoundedCornerShape(16.dp)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = segmentShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { idx ->
                val date = mondayStart.plusDays(idx.toLong())
                val isSelected = date == selectedDay
                val dayAbbrev = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
                val dayNum = date.dayOfMonth.toString()

                val cellShape = when (idx) {
                    0 -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    5 -> RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    else -> RoundedCornerShape(8.dp)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = if (isSelected) lightBlue else MaterialTheme.colorScheme.surface,
                            shape = cellShape
                        )
                        .clickable { onSelect(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dayAbbrev, style = MaterialTheme.typography.labelMedium)
                        Text(dayNum, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (idx < 5) {
                    Spacer(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

/* ---- Day header ---- */
@Composable
private fun DayHeader(
    selectedDay: LocalDate,
    today: LocalDate,
    hasBookings: Boolean,
    onDownload: () -> Unit
) {
    val blue = Color(0xFF0D6EFD)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (selectedDay == today) "Today"
        else selectedDay.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

        Column(modifier = Modifier.width(IntrinsicSize.Min)) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(blue)
            )
        }

        Spacer(Modifier.weight(1f))
        // (Download icon intentionally removed)
    }
}

/* ---- Booking row (Lab): two-column card ---- */
@Composable
private fun BookingRowTwoColumn(
    start: String?,
    end: String?,
    description: String?,
    bookedBy: String
) {
    val lightBlue = Color(0xFFCAF5F6)
    val shape = RoundedCornerShape(0.dp)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(2.dp, Color.White), shape),
        shape = shape,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = BOOKING_CARD_ELEVATION
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(0.dp)
        ) {
            // Left: times (white)
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .padding(14.dp)
            ) {
                Text(
                    text = "${hhmm(start)} – ${hhmm(end)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            // Right: details (light blue)
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .background(color = lightBlue)
                    .padding(14.dp)
            ) {
                Text(
                    text = description?.takeIf { it.isNotBlank() } ?: "No description",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(BOOKING_TEXT_GAP))
                prettyBookedBy(bookedBy)?.let { name ->
                    Text(
                        text = "Booked by: $name",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

/* ------------------------- CLASS TAB (unchanged visuals) ------------------------- */
// (Class tab left as-is; it already behaves correctly)

@Composable
private fun ClassTab(
    loading: Boolean,
    error: String?,
    items: List<ClassScheduleItem>,
    onRetry: () -> Unit,
    onDownloadAll: () -> Unit
) {
    val todayDoW = remember { LocalDate.now().dayOfWeek }
    var selectedDoW by remember { mutableStateOf(todayOrMonday(todayDoW)) }

    val normalized = remember(items) {
        val order = mapOf(
            "monday" to 1, "tuesday" to 2, "wednesday" to 3,
            "thursday" to 4, "friday" to 5
        )
        items
            .filter { it.weekDay.lowercase(Locale.ROOT) in order.keys }
            .sortedWith(
                compareBy<ClassScheduleItem>(
                    { order[it.weekDay.lowercase(Locale.ROOT)] ?: 99 },
                    { it.startTime },
                    { it.moduleCode }
                )
            )
    }

    val selectedKey = selectedDoW.name.lowercase(Locale.ROOT)
    val dayNamePretty = selectedDoW.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val isToday = selectedDoW == todayDoW
    val dayItems = remember(normalized, selectedKey) {
        normalized.filter { it.weekDay.equals(dayNamePretty, ignoreCase = true) }
    }
    val hasClasses = dayItems.isNotEmpty()

    Box(Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            error != null -> ErrorBox(error, onRetry)
            items.isEmpty() -> EmptyBox("No classes for this semester.")
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    ClassWeekStrip(
                        selected = selectedDoW,
                        onSelect = { selectedDoW = it }
                    )
                    Spacer(Modifier.height(12.dp))

                    ClassDayHeader(
                        isToday = isToday,
                        dayNamePretty = dayNamePretty,
                        hasClasses = hasClasses,
                        onDownload = onDownloadAll
                    )

                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (dayItems.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No classes for this day.", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        } else {
                            items(dayItems) { c ->
                                ClassRowTwoColumn(
                                    start = c.startTime,
                                    end = c.endTime,
                                    moduleCode = c.moduleCode,
                                    venue = c.venue
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
private fun ClassWeekStrip(
    selected: DayOfWeek,
    onSelect: (DayOfWeek) -> Unit
) {
    val lightBlue = Color(0xFFCAF5F6)
    val days = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WEEKDAY_CORNER)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(WEEKDAY_CELL_HEIGHT)
                .padding(horizontal = WEEKDAY_GAP, vertical = WEEKDAY_GAP),
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEachIndexed { idx, dow ->
                val isSelected = dow == selected
                val cellShape = when (idx) {
                    0 -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    days.lastIndex -> RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    else -> RoundedCornerShape(8.dp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = if (isSelected) lightBlue else MaterialTheme.colorScheme.surface,
                            shape = cellShape
                        )
                        .clickable { onSelect(dow) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
                if (idx < days.lastIndex) Spacer(Modifier.width(WEEKDAY_GAP))
            }
        }
    }
}

@Composable
private fun ClassDayHeader(
    isToday: Boolean,
    dayNamePretty: String,
    hasClasses: Boolean,
    onDownload: () -> Unit
) {
    val blue = Color(0xFF0D6EFD)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (isToday) "Today" else dayNamePretty

        Column(modifier = Modifier.width(IntrinsicSize.Min)) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(blue)
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun ClassRowTwoColumn(
    start: String?,
    end: String?,
    moduleCode: String?,
    venue: String?
) {
    val lightBlue = Color(0xFFCAF5F6)
    val shape = RoundedCornerShape(0.dp)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(2.dp, Color.White), shape),
        shape = shape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = CLASS_CARD_ELEVATION)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .padding(14.dp)
            ) {
                Text(
                    text = "${hhmm(start)} – ${hhmm(end)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .background(color = lightBlue)
                    .padding(14.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = moduleCode ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = MODULE_CODE_FONT_SIZE),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(MODULE_VENUE_GAP))

                Row(
                    modifier = Modifier.align(Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "Venue",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .offset(x = VENUE_ICON_START_NUDGE)
                            .size(VENUE_ICON_SIZE)
                    )
                    if (VENUE_TEXT_GAP > 0.dp) Spacer(Modifier.width(VENUE_TEXT_GAP))
                    val venueUpper = (venue ?: "").uppercase(Locale.getDefault())
                    Text(
                        text = venueUpper,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = VENUE_TEXT_FONT_SIZE)
                    )
                }
            }
        }
    }
}

/* ----------------------- ASSESSMENTS TAB (UPDATED) ----------------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AssessmentTab(
    loading: Boolean,
    error: String?,
    items: List<Assessment>,
    onRetry: () -> Unit,
    onDownloadAll: () -> Unit
) {
    val ordered = remember(items) {
        items.sortedWith(
            compareBy<Assessment>({ it.date }, { it.startTime ?: it.dueTime ?: "" }, { it.title })
        )
    }

    val today = LocalDate.now()
    val mondayIfSunday = remember(today) { if (today.dayOfWeek == DayOfWeek.SUNDAY) today.plusDays(1) else today }
    val defaultDow = remember(mondayIfSunday) { mondayIfSunday.dayOfWeek }

    Box(Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            error != null -> ErrorBox(error, onRetry)
            ordered.isEmpty() -> EmptyBox("No assessments for this semester.")
            else -> {
                val availableMonths: List<YearMonth> = remember(ordered) {
                    val months = ordered.mapNotNull { parseLocalDate(it.date) }
                        .map { YearMonth.from(it) }
                        .distinct()
                        .sorted()
                    if (months.isEmpty()) listOf(YearMonth.now()) else months
                }

                var monthIndex by remember(availableMonths, mondayIfSunday) {
                    val autoYm = YearMonth.from(mondayIfSunday)
                    val idx = availableMonths.indexOf(autoYm).let { if (it >= 0) it else 0 }
                    mutableStateOf(idx)
                }
                val selectedMonth = availableMonths[monthIndex.coerceIn(0, availableMonths.lastIndex)]

                var selectedDoW by remember { mutableStateOf(defaultDow) }

                val dayItems = remember(ordered, selectedMonth, selectedDoW) {
                    ordered.filter { a ->
                        parseLocalDate(a.date)?.let { d ->
                            YearMonth.from(d) == selectedMonth && d.dayOfWeek == selectedDoW
                        } ?: false
                    }.sortedWith(compareBy({ it.date }, { it.startTime ?: it.dueTime ?: "" }))
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Top (collapsible away)
                    item(key = "assess-month-picker") {
                        AssessmentMonthPicker(
                            month = selectedMonth,
                            canPrev = monthIndex > 0,
                            canNext = monthIndex < availableMonths.lastIndex,
                            onPrev = { if (monthIndex > 0) monthIndex-- },
                            onNext = { if (monthIndex < availableMonths.lastIndex) monthIndex++ }
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // Pinned (weekday picker + day header)
                    stickyHeader(key = "assess-sticky") {
                        Surface(color = MaterialTheme.colorScheme.background) {
                            Column(Modifier.fillMaxWidth()) {
                                AssessmentWeekStrip(
                                    selected = selectedDoW,
                                    onSelect = { selectedDoW = it }
                                )
                                Spacer(Modifier.height(12.dp))
                                AssessmentDayHeader(
                                    dayNamePretty = selectedDoW.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                    hasAny = ordered.isNotEmpty(),
                                    onDownloadAll = onDownloadAll
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }

                    // Cards list
                    if (dayItems.isEmpty()) {
                        item(key = "assess-empty-day") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No assessments for this weekday in ${
                                        selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                                    }. ",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        items(dayItems, key = { it.id ?: (it.title + it.date).hashCode() }) { a ->
                            AssessmentRowTwoColumn(
                                date = a.date,
                                start = a.startTime,
                                end = a.endTime,
                                due = a.dueTime,
                                moduleCode = a.moduleCode,
                                title = a.title,
                                venue = a.venue,
                                description = a.description
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssessmentMonthPicker(
    month: YearMonth,
    canPrev: Boolean,
    canNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val title = remember(month) {
        "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrev,
            enabled = canPrev
        ) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month", tint = Color.Unspecified)
        }
        Spacer(Modifier.weight(1f))
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onNext,
            enabled = canNext
        ) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next month", tint = Color.Unspecified)
        }
    }
}

@Composable
private fun AssessmentWeekStrip(
    selected: DayOfWeek,
    onSelect: (DayOfWeek) -> Unit
) {
    val lightBlue = Color(0xFFCAF5F6)
    val days = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WEEKDAY_CORNER)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(WEEKDAY_CELL_HEIGHT)
                .padding(horizontal = WEEKDAY_GAP, vertical = WEEKDAY_GAP),
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEachIndexed { idx, dow ->
                val isSelected = dow == selected
                val cellShape = when (idx) {
                    0 -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    days.lastIndex -> RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    else -> RoundedCornerShape(8.dp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = if (isSelected) lightBlue else MaterialTheme.colorScheme.surface,
                            shape = cellShape
                        )
                        .clickable { onSelect(dow) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
                if (idx < days.lastIndex) Spacer(Modifier.width(WEEKDAY_GAP))
            }
        }
    }
}

@Composable
private fun AssessmentDayHeader(
    dayNamePretty: String,
    hasAny: Boolean,
    onDownloadAll: () -> Unit
) {
    val blue = Color(0xFF0D6EFD)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(IntrinsicSize.Min)) {
            Text(
                text = dayNamePretty,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(blue)
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun AssessmentRowTwoColumn(
    date: String?,
    start: String?,
    end: String?,
    due: String?,
    moduleCode: String?,
    title: String,
    venue: String?,
    description: String?
) {
    val lightBlue = Color(0xFFCAF5F6)
    var showDesc by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(0.dp)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(2.dp, Color.White), shape),
        shape = shape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = CLASS_CARD_ELEVATION)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .padding(14.dp)
            ) {
                val dateText = date?.let { formatDate(it) }.orEmpty()
                if (dateText.isNotEmpty()) {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D6EFD)
                    )
                    Spacer(Modifier.height(6.dp))
                }
                val timeText = when {
                    !start.isNullOrBlank() || !end.isNullOrBlank() -> "${hhmm(start)} – ${hhmm(end)}"
                    !due.isNullOrBlank() -> "Due: ${hhmm(due)}"
                    else -> ""
                }
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Left
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .background(color = lightBlue)
                    .padding(14.dp)
            ) {
                Text(
                    text = moduleCode ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = MODULE_CODE_FONT_SIZE),
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(MODULE_VENUE_GAP))

                val venueText = venue?.trim().orEmpty()
                if (venueText.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = "Venue",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .offset(x = VENUE_ICON_START_NUDGE)
                                .size(VENUE_ICON_SIZE)
                        )
                        if (VENUE_TEXT_GAP > 0.dp) Spacer(Modifier.width(VENUE_TEXT_GAP))
                        Text(
                            text = venueText.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = VENUE_TEXT_FONT_SIZE)
                        )
                    }
                }

                if (!description.isNullOrBlank() && showDesc) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Justify
                    )
                }

                Spacer(Modifier.weight(1f))

                if (!description.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDesc = !showDesc },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End as Arrangement.Horizontal
                    ) {
                        Text(
                            text = if (showDesc) "Hide description" else "Show description",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = if (showDesc) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (showDesc) "Hide description" else "Show description",
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }
    }
}

/* --------------------------- Shared UI --------------------------- */

@Composable
private fun ErrorBox(msg: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(msg, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun EmptyBox(msg: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(msg, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun formatDate(iso: String): String = try {
    val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    outFmt.format(inFmt.parse(iso)!!)
} catch (_: Throwable) { iso }

/* --------------------------- PDF helpers --------------------------- */
private fun labPdfData(items: List<LabBooking>): Triple<String, List<String>, List<List<String>>> {
    val title = "Lab Schedule"
    val headers = listOf("Date", "Day(s)", "Start", "End", "Booked By", "Description")
    val rows = items.map {
        val fullName = listOfNotNull(
            it.firstName?.trim().takeUnless { fn -> fn.isNullOrEmpty() },
            it.lastName?.trim().takeUnless { ln -> ln.isNullOrEmpty() }
        ).joinToString(" ").ifBlank { it.userName }
        listOf(
            it.bookingDate,
            it.weekDays,
            hhmm(it.startTime),
            hhmm(it.endTime),
            fullName,
            it.description ?: ""
        )
    }
    return Triple(title, headers, rows)
}

private fun classPdfData(items: List<ClassScheduleItem>): Triple<String, List<String>, List<List<String>>> {
    val title = "Class Timetable"
    val headers = listOf("Day", "Start", "End", "Module", "Name", "Venue")
    val rows = items.map {
        listOf(it.weekDay, hhmm(it.startTime), hhmm(it.endTime), it.moduleCode, it.moduleName, it.venue)
    }
    return Triple(title, headers, rows)
}

private fun classPdfDataForDay(items: List<ClassScheduleItem>, weekDay: String): Triple<String, List<String>, List<List<String>>> {
    val filtered = items.filter { it.weekDay.equals(weekDay, ignoreCase = true) }
    val title = "Class Timetable • $weekDay"
    val headers = listOf("Day", "Start", "End", "Module", "Name", "Venue")
    val rows = filtered.map {
        listOf(it.weekDay, hhmm(it.startTime), hhmm(it.endTime), it.moduleCode, it.moduleName, it.venue)
    }
    return Triple(title, headers, rows)
}

private fun assessmentPdfData(items: List<Assessment>): Triple<String, List<String>, List<List<String>>> {
    val title = "Assessments"
    val headers = listOf("Date", "Start", "End/Due", "Title", "Venue", "Timed")
    val rows = items.map {
        listOf(
            it.date,
            it.startTime ?: "",
            it.endTime ?: it.dueTime ?: "",
            it.title,
            it.venue ?: "",
            if (it.isTimed) "Yes" else "No"
        )
    }
    return Triple(title, headers, rows)
}

/* --------------------------- Utils --------------------------- */
// Show HH:mm even if value is HH:mm:ss
private fun hhmm(time: String?): String {
    if (time == null) return ""
    return try {
        if (time.length >= 5) time.substring(0, 5) else time
    } catch (_: Throwable) {
        time
    }
}

// Hide numeric-only student numbers; show full name when provided.
private fun prettyBookedBy(raw: String): String? {
    val looksNumeric = raw.isNotBlank() && raw.all { it.isDigit() } && raw.length in 6..12
    return if (looksNumeric) null else raw
}

// Helper to force Monday when today is Saturday or Sunday (Class UI shows Mon–Fri only)
private fun todayOrMonday(today: DayOfWeek): DayOfWeek {
    return if (today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY) DayOfWeek.MONDAY else today
}

// Parse ISO date safely
private fun parseLocalDate(iso: String?): LocalDate? = try {
    if (iso.isNullOrBlank()) null else LocalDate.parse(iso)
} catch (_: Throwable) { null }

/* ---- NEW: Semester week-window helpers for Lab navigation ---- */
private fun semesterWeekWindow(year: Int, semester: Int): Pair<LocalDate, LocalDate> {
    return if (semester == 1) {
        val start = LocalDate.of(year, 1, 1).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
        val end = LocalDate.of(year, 6, 30).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        start to end
    } else {
        val start = LocalDate.of(year, 7, 1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val end = LocalDate.of(year, 12, 31).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        start to end
    }
}

private fun clampWeekToWindow(candidate: LocalDate, year: Int, semester: Int): LocalDate {
    val (minW, maxW) = semesterWeekWindow(year, semester)
    return when {
        candidate.isBefore(minW) -> minW
        candidate.isAfter(maxW) -> maxW
        else -> candidate
    }
}
