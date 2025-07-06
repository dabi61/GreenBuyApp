package com.example.greenbuyapp.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.notice.model.Notice
import com.example.greenbuyapp.domain.notice.NoticeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val noticeRepository: NoticeRepository
) : ViewModel() {

    private val _deliveredNotices = MutableStateFlow<List<Notice>>(emptyList())
    val pendingNotices: StateFlow<List<Notice>> = _deliveredNotices

    fun loadPendingNotices() {
        viewModelScope.launch {
            try {
                val result = noticeRepository.getDeliveredNotices()
                _deliveredNotices.value = result
            } catch (e: Exception) {
                // TODO: xử lý lỗi nếu cần
                _deliveredNotices.value = emptyList()
            }
        }
    }
}
