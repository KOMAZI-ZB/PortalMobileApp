package com.example.portalapp.data.repository

import com.example.portalapp.data.mappers.toModel
import com.example.portalapp.models.FaqEntry
import com.example.portalapp.network.FaqApi
import com.example.portalapp.util.Http
import com.example.portalapp.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaqRepository @Inject constructor(
    private val api: FaqApi
) {
    suspend fun fetch(page: Int = 1, size: Int = 50, search: String? = null): Result<List<FaqEntry>> = try {
        val dtos = api.getFaqs(pageNumber = page, pageSize = size, searchTerm = search)
        Result.Success(dtos.map { it.toModel() })
    } catch (t: Throwable) {
        Result.Error(Http.friendlyMessage(t))
    }
}
