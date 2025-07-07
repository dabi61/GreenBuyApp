package com.example.greenbuyapp.domain.notice

import com.example.greenbuyapp.data.notice.NoticeService
import com.example.greenbuyapp.data.notice.model.Notice
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoticeRepository(
    private val noticeService: NoticeService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun getDeliveredNotices(): List<Notice> = withContext(dispatcher) {
        val response = noticeService.getOrder(search = "delivered")
        return@withContext response.items
    }
}