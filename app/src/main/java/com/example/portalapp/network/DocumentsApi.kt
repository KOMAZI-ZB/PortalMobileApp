package com.example.portalapp.network

import com.example.portalapp.data.dto.DocumentDto
import retrofit2.http.GET
import retrofit2.http.Path

interface DocumentsApi {
    // GET api/Documents/module/{moduleId}
    @GET("api/Documents/module/{moduleId}")
    suspend fun byModule(@Path("moduleId") moduleId: Int): List<DocumentDto>
}
