package com.example.portalapp.models


data class LabBooking(
    val id: Int,
    val userName: String,
    val firstName: String?,         // ← NEW
    val lastName: String?,          // ← NEW
    val weekDays: String,
    val startTime: String,
    val endTime: String,
    val description: String?,
    val bookingDate: String
)

data class ClassScheduleItem(
    val moduleCode: String,
    val moduleName: String,
    val venue: String,
    val weekDay: String,
    val startTime: String,
    val endTime: String
)
