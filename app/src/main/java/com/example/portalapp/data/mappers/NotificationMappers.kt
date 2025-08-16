package com.example.portalapp.data.mappers

import com.example.portalapp.data.dto.NotificationDto
import com.example.portalapp.models.Notification

fun NotificationDto.toModel(): Notification = Notification(
    id = id,
    type = type,
    title = title,
    message = message,
    imagePath = imagePath,
    createdBy = createdBy,
    createdAt = createdAt,
    moduleId = moduleId,
    audience = audience,
    isRead = isRead ?: false
)
