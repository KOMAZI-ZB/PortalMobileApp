package com.example.portalapp.data.dto

import com.squareup.moshi.Json

data class ModuleDto(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "moduleCode") val moduleCode: String,
    @field:Json(name = "moduleName") val moduleName: String,
    @field:Json(name = "semester") val semester: Int,
    @field:Json(name = "isYearModule") val isYearModule: Boolean = false,
    @field:Json(name = "classVenue") val classVenue: String? = null,
    @field:Json(name = "weekDays") val weekDays: List<String>? = null,
    @field:Json(name = "startTimes") val startTimes: List<String>? = null,
    @field:Json(name = "endTimes") val endTimes: List<String>? = null,
    @field:Json(name = "classSessions") val classSessions: List<ClassSessionDto>? = null,
    @field:Json(name = "assessments") val assessments: List<AssessmentDto>? = null
)

data class ClassSessionDto(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "venue") val venue: String,
    @field:Json(name = "weekDay") val weekDay: String,
    @field:Json(name = "startTime") val startTime: String,
    @field:Json(name = "endTime") val endTime: String
)

data class AssessmentDto(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "title") val title: String,
    @field:Json(name = "date") val date: String,           // yyyy-MM-dd
    @field:Json(name = "startTime") val startTime: String? = null,
    @field:Json(name = "endTime") val endTime: String? = null,
    @field:Json(name = "dueTime") val dueTime: String? = null,
    @field:Json(name = "venue") val venue: String? = null,
    @field:Json(name = "isTimed") val isTimed: Boolean = false
)
