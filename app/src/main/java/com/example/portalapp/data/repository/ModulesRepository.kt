package com.example.portalapp.data.repository

import com.example.portalapp.data.mappers.toModel
import com.example.portalapp.models.Assessment
import com.example.portalapp.models.Module
import com.example.portalapp.network.ModulesApi
import com.example.portalapp.util.Http
import com.example.portalapp.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModulesRepository @Inject constructor(
    private val api: ModulesApi
) {

    suspend fun bySemester(semester: Int): Result<List<Module>> = try {
        val dtos = api.bySemester(semester)
        Result.Success(dtos.map { it.toModel() })
    } catch (t: Throwable) {
        Result.Error(Http.friendlyMessage(t))
    }

    suspend fun get(id: Int): Result<Module> = try {
        val dto = api.get(id)
        Result.Success(dto.toModel())
    } catch (t: Throwable) {
        Result.Error(Http.friendlyMessage(t))
    }

    suspend fun assessments(moduleId: Int): Result<List<Assessment>> = try {
        val dtos = api.assessments(moduleId)
        Result.Success(dtos.map { it.toModel() })
    } catch (t: Throwable) {
        Result.Error(Http.friendlyMessage(t))
    }
}
