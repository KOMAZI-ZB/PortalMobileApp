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

@Singleton
class SchedulerRepository @Inject constructor(
    private val api: SchedulerApi,
    private val prefs: UserPrefs
) {
    private suspend fun isPrivileged(): Boolean {
        val roles = prefs.rolesFlow().first()
        return roles.any { it.equals("Admin", true) || it.equals("Coordinator", true) }
    }

    suspend fun labBookings(): Result<List<LabBooking>> = try {
        val dtos = if (isPrivileged()) api.getAllLabBookings() else api.getMyLabBookings()
        Result.Success(dtos.map { it.toModel() })
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
