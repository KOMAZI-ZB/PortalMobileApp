package com.example.portalapp.data.mappers

import com.example.portalapp.data.dto.DocumentDto
import com.example.portalapp.models.Document

fun DocumentDto.toModel(): Document = Document(
    id = id,
    title = title.trim(),
    fileUrl = filePath,         // backend provides a Cloudinary URL
    uploadedAt = uploadedAt,
    uploadedBy = uploadedBy,
    uploadedByUserName = uploadedByUserName,
    moduleId = moduleId,
    source = source?.trim().orEmpty()
)
