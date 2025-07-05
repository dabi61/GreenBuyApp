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

    private val _pendingNotices = MutableStateFlow<List<Notice>>(emptyList())
    val pendingNotices: StateFlow<List<Notice>> = _pendingNotices

    fun loadPendingNotices() {
        viewModelScope.launch {
            try {
                val result = noticeRepository.getPendingNotices()
                _pendingNotices.value = result
            } catch (e: Exception) {
                // TODO: xử lý lỗi nếu cần
                _pendingNotices.value = emptyList()
            }
        }
    }
}
