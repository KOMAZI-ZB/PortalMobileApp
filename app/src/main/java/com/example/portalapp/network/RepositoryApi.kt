package com.example.portalapp.network

import com.example.portalapp.data.dto.DocumentDto
import com.example.portalapp.data.dto.RepositoryDto
import retrofit2.http.GET
import retrofit2.http.Query

interface RepositoryApi {

    // External repository cards (public, but we call with auth header anyway)
    @GET("api/Repository/external")
    suspend fun getExternalRepositories(
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): List<RepositoryDto>

    // Internal repository files (auth required)
    @GET("api/Repository")
    suspend fun getInternalRepositoryDocs(
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): List<DocumentDto>
}
