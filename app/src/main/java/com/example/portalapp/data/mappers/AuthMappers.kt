package com.example.portalapp.data.mappers

import com.example.portalapp.data.dto.ModuleMiniDto
import com.example.portalapp.data.dto.UserDto
import com.example.portalapp.models.ModuleMini
import com.example.portalapp.models.User

fun UserDto.toModel(): User = User(
    userName = (userName ?: "").trim(),
    name = name?.trim().orEmpty(),
    surname = surname?.trim().orEmpty(),
    email = email?.trim().orEmpty(),
    token = token?.trim().orEmpty(),
    roles = roles?.filterNotNull() ?: emptyList(),
    joinDate = joinDate?.trim().orEmpty(),
    modules = modules?.map { it.toModel() } ?: emptyList()
)

fun ModuleMiniDto.toModel(): ModuleMini = ModuleMini(
    id = id ?: 0,
    code = code?.trim().orEmpty(),
    name = name?.trim().orEmpty()
)
