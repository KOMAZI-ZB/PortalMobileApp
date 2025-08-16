package com.example.portalapp.models

data class Module(
    val id: Int,
    val code: String,
    val name: String,
    val semester: Int,
    val isYearModule: Boolean,
    val classVenue: String?,
    val weekDays: List<String>,
    val startTimes: List<String>,
    val endTimes: List<String>,
    val sessions: List<ClassSession> = emptyList(),
    val assessments: List<Assessment> = emptyList()
)

data class ClassSession(
    val id: Int,
    val venue: String,
    val weekDay: String,
    val startTime: String,
    val endTime: String
)

data class Assessment(
    val id: Int,
    val title: String,
    val date: String,               // yyyy-MM-dd
    val startTime: String? = null,
    val endTime: String? = null,
    val dueTime: String? = null,
    val venue: String? = null,
    val isTimed: Boolean = false
)
