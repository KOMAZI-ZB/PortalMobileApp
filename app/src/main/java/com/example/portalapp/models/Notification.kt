package com.example.portalapp.models

data class Notification(
    val id: Int,
    val type: String,
    val title: String,
    val message: String,
    val imagePath: String?,
    val createdBy: String,
    val createdAt: String,
    val moduleId: Int?,
    val audience: String,
    val isRead: Boolean
)
