package com.example.portalapp.data.dto

import com.squareup.moshi.Json

data class RepositoryDto(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "label") val label: String,
    @field:Json(name = "linkUrl") val linkUrl: String,
    @field:Json(name = "imageUrl") val imageUrl: String
)
