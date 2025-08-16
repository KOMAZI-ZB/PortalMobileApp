package com.example.portalapp.data.repository

import com.example.portalapp.data.mappers.toModel
import com.example.portalapp.models.Notification
import com.example.portalapp.network.NotificationsApi
import com.example.portalapp.util.Http
import com.example.portalapp.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor(
    private val api: NotificationsApi
) {
    suspend fun fetch(page: Int = 1, size: Int = 20): Result<List<Notification>> = try {
        val dtos = api.getNotifications(page, size)
        Result.Success(dtos.map { it.toModel() })
    } catch (t: Throwable) {
        Result.Error(Http.friendlyMessage(t))
    }

    suspend fun markRead(id: Int): Result<Unit> = try {
        api.markRead(id)
        Result.Success(Unit)
    } catch (t: Throwable) {
        Result.Error(Http.friendlyMessage(t))
    }
}
