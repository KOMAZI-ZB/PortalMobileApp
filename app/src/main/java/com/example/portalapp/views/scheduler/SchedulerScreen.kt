package com.example.portalapp.views.scheduler

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.models.Assessment
import com.example.portalapp.models.ClassScheduleItem
import com.example.portalapp.models.LabBooking
import com.example.portalapp.util.PdfUtils
import com.example.portalapp.viewmodels.SchedulerViewModel
import java.text.SimpleDateFormat
import java.util.Locale

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

    Column(Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = state.selectedTab,
            modifier = Modifier.fillMaxWidth(),
            indicator = { positions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(positions[state.selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(selected = state.selectedTab == 0, onClick = { vm.onTabChange(0) }, text = { Text("Lab") })
            Tab(selected = state.selectedTab == 1, onClick = { vm.onTabChange(1) }, text = { Text("Class") })
            Tab(selected = state.selectedTab == 2, onClick = { vm.onTabChange(2) }, text = { Text("Assessments") })
        }

        if (state.selectedTab != 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Semester:", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = state.semester == 1, onClick = { vm.setSemester(1) }, label = { Text("1") })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = state.semester == 2, onClick = { vm.setSemester(2) }, label = { Text("2") })
                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        val (title, headers, rows) = when (state.selectedTab) {
                            1 -> classPdfData(state.classes)
                            2 -> assessmentPdfData(state.assessments)
                            else -> classPdfData(emptyList())
                        }
                        val bytes = PdfUtils.buildSimpleTablePdf(title, headers, rows)
                        pendingPdfBytes = bytes
                        val fileName = when (state.selectedTab) {
                            1 -> "Class_Timetable_Sem${state.semester}.pdf"
                            2 -> "Assessments_Sem${state.semester}.pdf"
                            else -> "Timetable.pdf"
                        }
                        createDoc.launch(fileName)
                    },
                    enabled = when (state.selectedTab) {
                        1 -> state.classes.isNotEmpty()
                        2 -> state.assessments.isNotEmpty()
                        else -> false
                    }
                ) { Text("Download") }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        val (title, headers, rows) = labPdfData(state.lab)
                        val bytes = PdfUtils.buildSimpleTablePdf(title, headers, rows)
                        pendingPdfBytes = bytes
                        createDoc.launch("Lab_Schedule.pdf")
                    },
                    enabled = state.lab.isNotEmpty()
                ) { Text("Download") }
            }
        }

        when (state.selectedTab) {
            0 -> LabTab(state.labLoading, state.labError, state.lab, onRetry = vm::refreshLab)
            1 -> ClassTab(state.classLoading, state.classError, state.classes, onRetry = vm::refreshClass)
            2 -> AssessmentTab(state.assessLoading, state.assessError, state.assessments, onRetry = vm::refreshAssessments)
        }
    }
}

@Composable
private fun LabTab(loading: Boolean, error: String?, items: List<LabBooking>, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            error != null -> ErrorBox(error, onRetry)
            items.isEmpty() -> EmptyBox("No lab bookings.")
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.id }) { b ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("${formatDate(b.bookingDate)} • ${b.weekDays}", fontWeight = FontWeight.SemiBold)
                                Text("${b.startTime} – ${b.endTime}")
                                if (!b.description.isNullOrBlank()) Text(b.description!!)
                                Text("Booked by: ${b.userName}", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassTab(loading: Boolean, error: String?, items: List<ClassScheduleItem>, onRetry: () -> Unit) {
    val ordered = remember(items) {
        val order = mapOf(
            "monday" to 1, "tuesday" to 2, "wednesday" to 3,
            "thursday" to 4, "friday" to 5, "saturday" to 6, "sunday" to 7
        )
        items.sortedWith(
            compareBy<ClassScheduleItem>(
                { order[it.weekDay.lowercase(Locale.ROOT)] ?: 99 },
                { it.startTime },
                { it.moduleCode }
            )
        )
    }

    Box(Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            error != null -> ErrorBox(error, onRetry)
            ordered.isEmpty() -> EmptyBox("No classes for this semester.")
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ordered) { c ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("${c.weekDay} • ${c.startTime} – ${c.endTime}", fontWeight = FontWeight.SemiBold)
                                Text("${c.moduleCode} • ${c.moduleName}")
                                Text("Venue: ${c.venue}", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssessmentTab(loading: Boolean, error: String?, items: List<Assessment>, onRetry: () -> Unit) {
    val ordered = remember(items) {
        items.sortedWith(compareBy<Assessment>({ it.date }, { it.startTime ?: it.dueTime ?: "" }))
    }

    Box(Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            error != null -> ErrorBox(error, onRetry)
            ordered.isEmpty() -> EmptyBox("No assessments for this semester.")
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ordered, key = { it.id ?: (it.title + it.date).hashCode() }) { a ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "${formatDate(a.date)}${a.startTime?.let { " • $it" } ?: ""}${a.endTime?.let { " – $it" } ?: ""}",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(a.title)
                                if (!a.venue.isNullOrBlank()) {
                                    Text("Venue: ${a.venue}", style = MaterialTheme.typography.labelMedium)
                                }
                                if (a.dueTime != null && a.startTime == null) {
                                    Text("Due: ${a.dueTime}", style = MaterialTheme.typography.labelMedium)
                                }
                                if (a.isTimed) {
                                    AssistChip(onClick = {}, label = { Text("Timed") }, enabled = false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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

/* -------- PDF helpers -------- */

private fun labPdfData(items: List<LabBooking>): Triple<String, List<String>, List<List<String>>> {
    val title = "Lab Schedule"
    val headers = listOf("Date", "Day(s)", "Start", "End", "Booked By", "Description")
    val rows = items.map {
        listOf(
            it.bookingDate,
            it.weekDays,
            it.startTime,
            it.endTime,
            it.userName,
            it.description ?: ""
        )
    }
    return Triple(title, headers, rows)
}

private fun classPdfData(items: List<ClassScheduleItem>): Triple<String, List<String>, List<List<String>>> {
    val title = "Class Timetable"
    val headers = listOf("Day", "Start", "End", "Module", "Name", "Venue")
    val rows = items.map {
        listOf(it.weekDay, it.startTime, it.endTime, it.moduleCode, it.moduleName, it.venue)
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
