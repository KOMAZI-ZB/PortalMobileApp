// file: network/AuthApi.kt
package com.example.portalapp.network

import com.example.portalapp.data.dto.LoginDto
import com.example.portalapp.data.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    // Base URL: https://csi-portal-app.azurewebsites.net/
    // Endpoint path keeps "api/" prefix.
    @POST("api/Account/login")
    suspend fun login(@Body body: LoginDto): UserDto
}
