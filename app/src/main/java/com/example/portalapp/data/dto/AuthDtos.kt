package com.example.portalapp.data.dto

import com.squareup.moshi.Json

data class LoginDto(
    @field:Json(name = "userName") val userName: String,
    @field:Json(name = "password") val password: String
)

data class UserDto(
    @field:Json(name = "userName") val userName: String?,
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "surname") val surname: String?,
    @field:Json(name = "email") val email: String?,
    @field:Json(name = "token") val token: String?,
    @field:Json(name = "roles") val roles: List<String>?,
    @field:Json(name = "joinDate") val joinDate: String?,
    @field:Json(name = "modules") val modules: List<ModuleMiniDto>? = emptyList()
)

data class ModuleMiniDto(
    @field:Json(name = "id") val id: Int? = null,
    @field:Json(name = "moduleCode") val code: String? = null,
    @field:Json(name = "moduleName") val name: String? = null
)
