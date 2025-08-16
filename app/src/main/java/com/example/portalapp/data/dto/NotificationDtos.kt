package com.example.portalapp.data.dto

import com.squareup.moshi.Json

data class NotificationDto(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "type") val type: String,
    @field:Json(name = "title") val title: String,
    @field:Json(name = "message") val message: String,
    @field:Json(name = "imagePath") val imagePath: String? = null,
    @field:Json(name = "createdBy") val createdBy: String,
    @field:Json(name = "createdAt") val createdAt: String,
    @field:Json(name = "moduleId") val moduleId: Int? = null,
    @field:Json(name = "audience") val audience: String = "All",
    // Service commonly includes read state for the current user
    @field:Json(name = "isRead") val isRead: Boolean? = null
)
