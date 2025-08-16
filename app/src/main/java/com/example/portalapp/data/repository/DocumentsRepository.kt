package com.example.portalapp.data.repository

import com.example.portalapp.data.mappers.toModel
import com.example.portalapp.models.Document
import com.example.portalapp.network.DocumentsApi
import com.example.portalapp.util.Http
import com.example.portalapp.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentsRepository @Inject constructor(
    private val api: DocumentsApi
) {
    suspend fun byModule(moduleId: Int): Result<List<Document>> = try {
        Result.Success(api.byModule(moduleId).map { it.toModel() })
    } catch (t: Throwable) {
        Result.Error(Http.friendlyMessage(t))
    }
}
