package com.example.portalapp.views.scheduler

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.R
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
private val CLASS_CARD_ELEVATION = 10.dp              // card lift for class items
private val MODULE_VENUE_GAP = 8.dp                   // vertical gap between module code and venue row

private val VENUE_ICON_SIZE = 16.dp                   // icon size (leave as-is)
private val VENUE_ICON_START_NUDGE = (-4).dp          // move icon left to align with module code’s left edge
private val VENUE_TEXT_GAP = 0.dp                     // gap between icon and “Venue:” text (0 = none)

// ✨ New font-size knobs
private val MODULE_CODE_FONT_SIZE = 17.sp             // ↔ increase/decrease module code size here
private val VENUE_TEXT_FONT_SIZE = 12.sp              // ↔ increase/decrease “Venue: …” text size here

private val WEEKDAY_CELL_HEIGHT = 56.dp               // height of weekday strip cells (Class)
private val WEEKDAY_GAP = 6.dp                        // gap between weekday cells
private val WEEKDAY_CORNER = 16.dp                    // rounding for strip container
private val SEMESTER_CHIP_GAP = 4.dp                  // tighter gap between "1" and "2"
private val SEMESTER_LABEL_SIZE = 16.sp               // slight font bump for semester chips
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
    val grey = Color(0xFF6B7280)

    Column(Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = state.selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = lightBlue,
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
                onClick = { vm.onTabChange(0) },
                text = {
                    Text(
                        "Lab",
                        fontWeight = if (state.selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (state.selectedTab == 0) blue else grey
                    )
                }
            )
            Tab(
                selected = state.selectedTab == 1,
                onClick = { vm.onTabChange(1) },
                text = {
                    Text(
                        "Class",
                        fontWeight = if (state.selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (state.selectedTab == 1) blue else grey
                    )
                }
            )
            Tab(
                selected = state.selectedTab == 2,
                onClick = { vm.onTabChange(2) },
                text = {
                    Text(
                        "Assessments",
                        fontWeight = if (state.selectedTab == 2) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (state.selectedTab == 2) blue else grey
                    )
                }
            )
        }

        // Top controls:
        // - Class: right-aligned Semester chips (no Download button).
        // - Assessments: Semester chips ONLY (download icon moves into the weekday header).
        when (state.selectedTab) {
            1 -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "Semester:",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = SEMESTER_LABEL_SIZE)
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = state.semester == 1,
                        onClick = { vm.setSemester(1) },
                        label = { Text("1", fontSize = SEMESTER_LABEL_SIZE) }
                    )
                    Spacer(Modifier.width(SEMESTER_CHIP_GAP))
                    FilterChip(
                        selected = state.semester == 2,
                        onClick = { vm.setSemester(2) },
                        label = { Text("2", fontSize = SEMESTER_LABEL_SIZE) }
                    )
                }
            }
            2 -> {
                // 👇 now identical to Class: right-aligned, same font sizes and gaps
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "Semester:",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = SEMESTER_LABEL_SIZE)
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = state.semester == 1,
                        onClick = { vm.setSemester(1) },
                        label = { Text("1", fontSize = SEMESTER_LABEL_SIZE) }
                    )
                    Spacer(Modifier.width(SEMESTER_CHIP_GAP))
                    FilterChip(
                        selected = state.semester == 2,
                        onClick = { vm.setSemester(2) },
                        label = { Text("2", fontSize = SEMESTER_LABEL_SIZE) }
                    )
                }
            }
        }

        when (state.selectedTab) {
            0 -> LabTab(
                loading = state.labLoading,
                error = state.labError,
                items = state.lab,
                onRetry = vm::refreshLab,
                onDownload = {
                    val (title, headers, rows) = labPdfData(state.lab)
                    val bytes = PdfUtils.buildSimpleTablePdf(title, headers, rows)
                    pendingPdfBytes = bytes
                    createDoc.launch("Lab_Schedule.pdf")
                }
            )
            1 -> ClassTab(
                loading = state.classLoading,
                error = state.classError,
                items = state.classes,
                onRetry = vm::refreshClass,
                // Always download the FULL class schedule (all days)
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
                    // Export FULL assessment schedule (all months/days)
                    val (title, headers, rows) = assessmentPdfData(state.assessments)
                    val bytes = PdfUtils.buildSimpleTablePdf(title, headers, rows)
                    pendingPdfBytes = bytes
                    createDoc.launch("Assessments_Sem${state.semester}.pdf")
                }
            )
        }
    }
}

/* ------------------------- LAB TAB (UNCHANGED) ------------------------- */

@Composable
private fun LabTab(
    loading: Boolean,
    error: String?,
    items: List<LabBooking>,
    onRetry: () -> Unit,
    onDownload: () -> Unit
) {
    val today = LocalDate.now()

    // Monday→Saturday window; if Sunday, show next week
    val mondayStart = remember(today) {
        if (today.dayOfWeek == DayOfWeek.SUNDAY) today.plusDays(1)
        else today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    var selectedDay by remember(mondayStart) {
        mutableStateOf(if (today.dayOfWeek == DayOfWeek.SUNDAY) mondayStart else today)
    }
    val monthTitle = remember(mondayStart) {
        mondayStart.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    Box(Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            error != null -> ErrorBox(error, onRetry)
            items.isEmpty() -> EmptyBox("No lab bookings.")
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    MonthTitle(monthTitle)
                    Spacer(Modifier.height(8.dp))
                    WeekStripCard(
                        mondayStart = mondayStart,
                        selectedDay = selectedDay,
                        onSelect = { selectedDay = it }
                    )
                    Spacer(Modifier.height(12.dp))

                    // Compute if this selected day has bookings (controls icon visibility)
                    val dateKey = remember(selectedDay) {
                        selectedDay.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    val hasBookings = remember(items, selectedDay) {
                        items.any { it.bookingDate == dateKey }
                    }

                    DayHeader(
                        selectedDay = selectedDay,
                        today = today,
                        hasBookings = hasBookings,
                        onDownload = onDownload
                    )

                    Spacer(Modifier.height(8.dp))

                    // List takes remaining space
                    Box(Modifier.weight(1f)) {
                        DayBookingsList(
                            all = items,
                            selectedDay = selectedDay
                        )
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

/* ---- Day header + optional download button ---- */
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

        // Underline matches exactly the text width
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

        if (hasBookings) {
            IconButton(
                onClick = onDownload,
                modifier = Modifier.size(DOWNLOAD_ICON_SIZE)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.download2),
                    contentDescription = "Download Lab Schedule",
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/* ---- Day bookings list (booked slots only) ---- */
@Composable
private fun DayBookingsList(
    all: List<LabBooking>,
    selectedDay: LocalDate
) {
    val iso = DateTimeFormatter.ISO_LOCAL_DATE
    val key = selectedDay.format(iso)

    val dayItems = remember(all, selectedDay) {
        all.filter { it.bookingDate == key }.sortedBy { it.startTime }
    }

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
            }
        }
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

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
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
                    // ✅ FIX: typTypography -> typography
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

/* ------------------------- CLASS TAB (UPDATED) ------------------------- */

@Composable
private fun ClassTab(
    loading: Boolean,
    error: String?,
    items: List<ClassScheduleItem>,
    onRetry: () -> Unit,
    onDownloadAll: () -> Unit
) {
    val todayDoW = remember { LocalDate.now().dayOfWeek }        // today’s weekday
    var selectedDoW by remember { mutableStateOf(todayOrMonday(todayDoW)) } // Mon–Sat only, no Sunday

    // Normalize items ordering and group by weekday for quick lookups
    val normalized = remember(items) {
        val order = mapOf(
            "monday" to 1, "tuesday" to 2, "wednesday" to 3,
            "thursday" to 4, "friday" to 5, "saturday" to 6
        )
        items
            .filter { it.weekDay.lowercase(Locale.ROOT) in order.keys } // ignore Sundays
            .sortedWith(
                compareBy<ClassScheduleItem>(
                    { order[it.weekDay.lowercase(Locale.ROOT)] ?: 99 },
                    { it.startTime },
                    { it.moduleCode }
                )
            )
    }

    // Current day’s classes
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
                    // Weekday strip (Mon–Sat), NO DATES
                    ClassWeekStrip(
                        selected = selectedDoW,
                        onSelect = { selectedDoW = it }
                    )
                    Spacer(Modifier.height(12.dp))

                    // Title row with “Today” logic + download ICON (no button)
                    ClassDayHeader(
                        isToday = isToday,
                        dayNamePretty = dayNamePretty,
                        hasClasses = hasClasses,
                        onDownload = onDownloadAll   // always download ALL classes
                    )

                    Spacer(Modifier.height(8.dp))

                    // List (two-column cards)
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

        // Underline matches exactly the text width
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

        if (hasClasses) {
            IconButton(
                onClick = onDownload,
                modifier = Modifier.size(DOWNLOAD_ICON_SIZE)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.download2),
                    contentDescription = "Download Class Schedule",
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
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

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = CLASS_CARD_ELEVATION)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(0.dp)
        ) {
            // Left: times (white), HH:mm only
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

            // Right: module code + venue (with icon), light blue
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .background(color = lightBlue)
                    .padding(14.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Module code (bold, bigger)
                Text(
                    text = moduleCode ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = MODULE_CODE_FONT_SIZE),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(MODULE_VENUE_GAP))

                // Venue row: icon + text flush-left; nudge icon left so its visible glyph aligns with module code
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

/* ----------------------- ASSESSMENTS TAB (REBUILT) ----------------------- */

@Composable
private fun AssessmentTab(
    loading: Boolean,
    error: String?,
    items: List<Assessment>,
    onRetry: () -> Unit,
    onDownloadAll: () -> Unit
) {
    // Pre-sort items by date then time for stable groupings
    val ordered = remember(items) {
        items.sortedWith(
            compareBy<Assessment>({ it.date }, { it.startTime ?: it.dueTime ?: "" }, { it.title })
        )
    }

    Box(Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            error != null -> ErrorBox(error, onRetry)
            ordered.isEmpty() -> EmptyBox("No assessments for this semester.")
            else -> {
                // Determine earliest assessment date
                val earliestDate = remember(ordered) {
                    ordered.mapNotNull { parseLocalDate(it.date) }.minOrNull()
                }

                // Build ALL 12 months for that year (Jan..Dec), default to earliest month
                val yearForMonths = earliestDate?.year ?: LocalDate.now().year
                val allMonths: List<YearMonth> = remember(yearForMonths) {
                    (1..12).map { m -> YearMonth.of(yearForMonths, m) }
                }
                var monthIndex by remember(allMonths, earliestDate) {
                    mutableStateOf((earliestDate?.monthValue ?: LocalDate.now().monthValue) - 1)
                }
                val selectedMonth = allMonths[monthIndex.coerceIn(0, allMonths.lastIndex)]

                // Weekday selection: default Monday (no "Today" logic here)
                var selectedDoW by remember { mutableStateOf(DayOfWeek.MONDAY) }

                // Filter: items in selected month AND on selected weekday
                val dayItems = remember(ordered, selectedMonth, selectedDoW) {
                    ordered.filter { a ->
                        parseLocalDate(a.date)?.let { d ->
                            YearMonth.from(d) == selectedMonth && d.dayOfWeek == selectedDoW
                        } ?: false
                    }.sortedWith(compareBy({ it.date }, { it.startTime ?: it.dueTime ?: "" }))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    // Month picker (chevrons) over the full-year month list
                    AssessmentMonthPicker(
                        month = selectedMonth,
                        canPrev = monthIndex > 0,
                        canNext = monthIndex < allMonths.lastIndex,
                        onPrev = { if (monthIndex > 0) monthIndex-- },
                        onNext = { if (monthIndex < allMonths.lastIndex) monthIndex++ }
                    )

                    Spacer(Modifier.height(8.dp))

                    // Weekday strip (Mon–Sat), no dates
                    AssessmentWeekStrip(
                        selected = selectedDoW,
                        onSelect = { selectedDoW = it }
                    )

                    Spacer(Modifier.height(12.dp))

                    // Header with selected weekday name (NO "Today") + Download ICON (always whole schedule)
                    AssessmentDayHeader(
                        dayNamePretty = selectedDoW.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        hasAny = ordered.isNotEmpty(),
                        onDownloadAll = onDownloadAll
                    )

                    Spacer(Modifier.height(8.dp))

                    // List (two-column cards)
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
                                    date = a.date,                 // ← show date first
                                    start = a.startTime,
                                    end = a.endTime,
                                    due = a.dueTime,
                                    moduleCode = a.moduleCode,   // leave right side unchanged
                                    title = a.title,
                                    venue = a.venue,
                                    description = a.description
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
private fun AssessmentMonthPicker(
    month: YearMonth,
    canPrev: Boolean,
    canNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val title = remember(month) {
        // e.g., "September 2025"
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
        // Underline matches exactly the text width (no "Today" label here)
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

        if (hasAny) {
            IconButton(
                onClick = onDownloadAll,
                modifier = Modifier.size(DOWNLOAD_ICON_SIZE)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.download2),
                    contentDescription = "Download Full Assessment Schedule",
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun AssessmentRowTwoColumn(
    date: String?,             // NEW: show the date first
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

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = CLASS_CARD_ELEVATION)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left: DATE first, then time/due
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .padding(14.dp)
            ) {
                val dateText = date?.let { formatDate(it) }.orEmpty()
                if (dateText.isNotEmpty()) {
                    Text(
                        text = dateText,                 // e.g., "19 Aug 2025"
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
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
                    maxLines = 1
                )
            }

            // Right: details column (light blue) — unchanged per your request
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .background(color = lightBlue)
                    .padding(14.dp)
            ) {
                // Module code (bold, bigger)
                Text(
                    text = moduleCode ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = MODULE_CODE_FONT_SIZE),
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(MODULE_VENUE_GAP))

                // Venue row (icon + uppercase text), same sizing as Class
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
                        text = (venue ?: "").uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = VENUE_TEXT_FONT_SIZE)
                    )
                }

                // Collapsible description (only if present)
                if (!description.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDesc = !showDesc },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (showDesc) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (showDesc) "Hide description" else "Show description",
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (showDesc) "Hide description" else "Show description",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (showDesc) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall
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

// Helper to force Monday when today is Sunday (Class UI ignores Sundays)
private fun todayOrMonday(today: DayOfWeek): DayOfWeek {
    return if (today == DayOfWeek.SUNDAY) DayOfWeek.MONDAY else today
}

// Parse ISO date safely
private fun parseLocalDate(iso: String?): LocalDate? = try {
    if (iso.isNullOrBlank()) null else LocalDate.parse(iso)
} catch (_: Throwable) { null }
