package com.example.portalapp.data.mappers

import com.example.portalapp.data.dto.FaqEntryDto
import com.example.portalapp.models.FaqEntry

fun FaqEntryDto.toModel(): FaqEntry = FaqEntry(
    id = id,
    question = question.trim(),
    answer = answer?.trim().orEmpty(),
    lastUpdated = lastUpdated?.trim().orEmpty()
)
