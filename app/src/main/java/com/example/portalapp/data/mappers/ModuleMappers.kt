package com.example.portalapp.data.mappers

import com.example.portalapp.data.dto.AssessmentDto
import com.example.portalapp.data.dto.ClassSessionDto
import com.example.portalapp.data.dto.ModuleDto
import com.example.portalapp.models.Assessment
import com.example.portalapp.models.ClassSession
import com.example.portalapp.models.Module

fun ModuleDto.toModel(): Module = Module(
    id = id,
    code = moduleCode.trim(),
    name = moduleName.trim(),
    semester = semester,
    isYearModule = isYearModule,
    classVenue = classVenue?.trim(),
    weekDays = weekDays ?: emptyList(),
    startTimes = startTimes ?: emptyList(),
    endTimes = endTimes ?: emptyList(),
    sessions = (classSessions ?: emptyList()).map { it.toModel() },
    assessments = (assessments ?: emptyList()).map { it.toModel() }
)

fun ClassSessionDto.toModel(): ClassSession = ClassSession(
    id = id,
    venue = venue.trim(),
    weekDay = weekDay.trim(),
    startTime = startTime.trim(),
    endTime = endTime.trim()
)

/* -------- Assessments (NEW) -------- */
fun AssessmentDto.toModel() = Assessment(
    id = id,
    title = title,
    description = description,   // ← NEW
    moduleCode = moduleCode,     // ← NEW
    date = date,
    startTime = startTime,
    endTime = endTime,
    dueTime = dueTime,
    venue = venue,
    isTimed = isTimed
)
