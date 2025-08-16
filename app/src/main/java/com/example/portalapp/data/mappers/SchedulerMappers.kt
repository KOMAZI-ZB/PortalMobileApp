package com.example.portalapp.data.mappers

import com.example.portalapp.data.dto.ClassScheduleDto
import com.example.portalapp.data.dto.LabBookingDto
import com.example.portalapp.models.ClassScheduleItem
import com.example.portalapp.models.LabBooking


fun LabBookingDto.toModel() = LabBooking(
    id = id,
    userName = userName,
    weekDays = weekDays,
    startTime = startTime,
    endTime = endTime,
    description = description,
    bookingDate = bookingDate
)

fun ClassScheduleDto.toModel() = ClassScheduleItem(
    moduleCode = moduleCode,
    moduleName = moduleName,
    venue = venue,
    weekDay = weekDay,
    startTime = startTime,
    endTime = endTime
)
