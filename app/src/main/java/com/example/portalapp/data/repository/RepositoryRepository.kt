package com.example.portalapp.data.repository

import com.example.portalapp.data.mappers.toModel
import com.example.portalapp.models.RepositoryLink
import com.example.portalapp.models.Document
import com.example.portalapp.network.RepositoryApi
import com.example.portalapp.util.Http
import com.example.portalapp.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryRepository @Inject constructor(
    private val api: RepositoryApi
) {
    suspend fun external(): Result<List<RepositoryLink>> = try {
        val dtos = api.getExternalRepositories()
        Result.Success(dtos.map { it.toModel() })
    } catch (t: Throwable) {
        Result.Error(Http.friendlyMessage(t))
    }

    suspend fun internal(): Result<List<Document>> = try {
        val dtos = api.getInternalRepositoryDocs()
        Result.Success(dtos.map { it.toModel() })
    } catch (t: Throwable) {
        Result.Error(Http.friendlyMessage(t))
    }
}
