// file: data/repository/AuthRepository.kt
package com.example.portalapp.data.repository

import com.example.portalapp.data.dto.LoginDto
import com.example.portalapp.data.mappers.toModel
import com.example.portalapp.models.User
import com.example.portalapp.network.AuthApi
import com.example.portalapp.util.Http
import com.example.portalapp.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi
) {
    suspend fun login(userName: String, password: String): Result<User> {
        return try {
            val dto = api.login(LoginDto(userName = userName, password = password))
            val user = dto.toModel()
            if (user.token.isBlank()) {
                Result.Error("Login failed: empty token.")
            } else {
                Result.Success(user)
            }
        } catch (t: Throwable) {
            Result.Error(Http.friendlyMessage(t))
        }
    }
}
