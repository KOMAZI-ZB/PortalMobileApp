package com.example.portalapp.data.dto

import com.squareup.moshi.Json

data class LabBookingDto(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "userName") val userName: String,
    @field:Json(name = "firstName") val firstName: String? = null,   // ← NEW
    @field:Json(name = "lastName") val lastName: String? = null,     // ← NEW
    @field:Json(name = "weekDays") val weekDays: String,
    @field:Json(name = "startTime") val startTime: String,           // "HH:mm" or "HH:mm:ss"
    @field:Json(name = "endTime") val endTime: String,
    @field:Json(name = "description") val description: String? = null,
    @field:Json(name = "bookingDate") val bookingDate: String        // "yyyy-MM-dd"
)

data class ClassScheduleDto(
    @field:Json(name = "moduleCode") val moduleCode: String,
    @field:Json(name = "moduleName") val moduleName: String,
    @field:Json(name = "venue") val venue: String,
    @field:Json(name = "weekDay") val weekDay: String,
    @field:Json(name = "startTime") val startTime: String,
    @field:Json(name = "endTime") val endTime: String
)
