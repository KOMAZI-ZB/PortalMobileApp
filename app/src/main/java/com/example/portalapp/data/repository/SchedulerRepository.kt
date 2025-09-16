package com.example.portalapp.data.repository

import com.example.portalapp.data.mappers.toModel
import com.example.portalapp.models.Assessment
import com.example.portalapp.models.ClassScheduleItem
import com.example.portalapp.models.LabBooking
import com.example.portalapp.network.SchedulerApi
import com.example.portalapp.storage.UserPrefs
import com.example.portalapp.util.Http
import com.example.portalapp.util.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

// 🗓️ java.time for reliable week math (Mon–Sat, Sundays excluded)
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Singleton
class SchedulerRepository @Inject constructor(
    private val api: SchedulerApi,
    private val prefs: UserPrefs
) {
    private suspend fun isPrivileged(): Boolean {
        val roles = prefs.rolesFlow().first()
        return roles.any { it.equals("Admin", true) || it.equals("Coordinator", true) }
    }

    // ⬇️ Fetch ALL lab bookings, then filter to this week (Mon–Sat). Sundays don't count.
    suspend fun labBookings(): Result<List<LabBooking>> = try {
        val dtos = api.getAllLabBookings()
        val all = dtos.map { it.toModel() }

        val today = LocalDate.now()
        val weekStart = if (today.dayOfWeek == DayOfWeek.SUNDAY) {
            // If it's Sunday, show the upcoming week's Mon–Sat
            today.plusDays(1)
        } else {
            // Otherwise, current week's Monday
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }
        val weekEnd = weekStart.plusDays(5) // Monday + 5 = Saturday

        val fmt = DateTimeFormatter.ISO_LOCAL_DATE // "yyyy-MM-dd"
        val filtered = all.filter { b ->
            try {
                val d = LocalDate.parse(b.bookingDate, fmt)
                !d.isBefore(weekStart) && !d.isAfter(weekEnd)
            } catch (_: Throwable) {
                false
            }
        }

        Result.Success(filtered)
    } catch (t: Throwable) {
        Result.Error(Http.friendlyMessage(t))
    }

    suspend fun classSchedule(semester: Int): Result<List<ClassScheduleItem>> = try {
        val dtos = api.getClassSchedule(semester)
        Result.Success(dtos.map { it.toModel() })
    } catch (t: Throwable) {
        Result.Error(Http.friendlyMessage(t))
    }

    suspend fun assessmentSchedule(semester: Int): Result<List<Assessment>> = try {
        val dtos = api.getAssessmentSchedule(semester)
        Result.Success(dtos.map { it.toModel() })
    } catch (t: Throwable) {
        Result.Error(Http.friendlyMessage(t))
    }
}
