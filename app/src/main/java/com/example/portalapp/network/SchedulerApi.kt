package com.example.portalapp.network

import com.example.portalapp.data.dto.AssessmentDto
import com.example.portalapp.data.dto.ClassScheduleDto
import com.example.portalapp.data.dto.LabBookingDto
import retrofit2.http.GET
import retrofit2.http.Path

interface SchedulerApi {
    @GET("api/Scheduler/lab")
    suspend fun getAllLabBookings(): List<LabBookingDto>

    @GET("api/Scheduler/lab/user")
    suspend fun getMyLabBookings(): List<LabBookingDto>

    @GET("api/Scheduler/class/{semester}")
    suspend fun getClassSchedule(@Path("semester") semester: Int): List<ClassScheduleDto>

    @GET("api/Scheduler/assessment/{semester}")
    suspend fun getAssessmentSchedule(@Path("semester") semester: Int): List<AssessmentDto>
}
