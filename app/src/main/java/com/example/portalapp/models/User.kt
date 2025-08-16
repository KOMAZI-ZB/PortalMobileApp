package com.example.portalapp.models


data class User(
    val userName: String,
    val name: String,
    val surname: String,
    val email: String,
    val token: String,
    val roles: List<String>,
    val joinDate: String?,
    val modules: List<ModuleMini> = emptyList()
)

data class ModuleMini(
    val id: Int?,
    val code: String?,
    val name: String?
)
