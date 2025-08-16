package com.example.portalapp.data.dto

import com.squareup.moshi.Json

data class DocumentDto(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "title") val title: String,
    @field:Json(name = "filePath") val filePath: String,
    @field:Json(name = "uploadedAt") val uploadedAt: String, // ISO string from backend
    @field:Json(name = "uploadedBy") val uploadedBy: String,
    @field:Json(name = "uploadedByUserName") val uploadedByUserName: String,
    @field:Json(name = "moduleId") val moduleId: Int?,
    @field:Json(name = "source") val source: String? = null
)
