package com.example.greenbuyapp.data.notice

import com.example.greenbuyapp.data.notice.model.NoticeListResponse
import retrofit2.http.GET;
import retrofit2.http.Query;

interface NoticeService {
    @GET("api/order/")
    suspend fun getOrder(
        @Query("status_filter")search: String? = null,
        @Query("page")page: Int? = null,
        @Query("limit")limit: Int? = null
    ): NoticeListResponse
}