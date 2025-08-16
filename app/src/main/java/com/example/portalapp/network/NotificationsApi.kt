package com.example.portalapp.network

import com.example.portalapp.data.dto.NotificationDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationsApi {

    // Backend sends a paged array and a "Pagination" header; we can ignore the header for v1
    @GET("api/Notifications")
    suspend fun getNotifications(
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): List<NotificationDto>

    @POST("api/Notifications/{id}/read")
    suspend fun markRead(@Path("id") id: Int): Response<ResponseBody>
}
