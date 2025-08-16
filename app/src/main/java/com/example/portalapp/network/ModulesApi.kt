package com.example.portalapp.network

import com.example.portalapp.data.dto.AssessmentDto
import com.example.portalapp.data.dto.ModuleDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ModulesApi {

    @GET("api/Modules/semester/{semester}")
    suspend fun bySemester(@Path("semester") semester: Int): List<ModuleDto>

    @GET("api/Modules/{id}")
    suspend fun get(@Path("id") id: Int): ModuleDto

    @GET("api/Modules/{id}/assessments")
    suspend fun assessments(@Path("id") id: Int): List<AssessmentDto>
}
