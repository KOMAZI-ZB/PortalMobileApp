package com.example.portalapp.network

import com.example.portalapp.data.dto.FaqEntryDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface FaqApi {

    // Backend returns a paginated list (array body + "Pagination" header).
    @GET("api/Faq")
    suspend fun getFaqs(
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("searchTerm") searchTerm: String? = null
    ): List<FaqEntryDto>

    // Admin endpoints (not used by current UI, but defined for completeness)
    @POST("api/Faq/create")
    suspend fun create(@Body dto: FaqEntryDto): Response<ResponseBody>

    @PUT("api/Faq/update/{id}")
    suspend fun update(@Path("id") id: Int, @Body dto: FaqEntryDto): Response<ResponseBody>

    @DELETE("api/Faq/{id}")
    suspend fun delete(@Path("id") id: Int): Response<ResponseBody>
}
