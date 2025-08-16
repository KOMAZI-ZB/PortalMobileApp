package com.example.portalapp.data.dto

import com.squareup.moshi.Json

data class FaqEntryDto(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "question") val question: String,
    @field:Json(name = "answer") val answer: String? = null,
    @field:Json(name = "lastUpdated") val lastUpdated: String? = null
)
