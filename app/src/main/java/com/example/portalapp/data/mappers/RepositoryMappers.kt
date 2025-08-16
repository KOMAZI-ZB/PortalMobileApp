package com.example.portalapp.data.mappers

import com.example.portalapp.data.dto.RepositoryDto
import com.example.portalapp.models.RepositoryLink

fun RepositoryDto.toModel(): RepositoryLink = RepositoryLink(
    id = id,
    label = label.trim(),
    linkUrl = linkUrl.trim(),
    imageUrl = imageUrl.trim()
)
