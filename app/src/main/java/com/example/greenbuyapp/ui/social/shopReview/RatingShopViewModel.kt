package com.example.greenbuyapp.ui.social.shopReview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.social.model.GetRatingShopResponse
import com.example.greenbuyapp.data.social.model.RatingShopRequest
import com.example.greenbuyapp.domain.social.FollowRepository
import com.example.greenbuyapp.util.Result // 👈 THÊM DÒNG NÀY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RatingShopViewModel(
    private val followRepository: FollowRepository
) : ViewModel() {

    private val _submitResult = MutableStateFlow<Result<GetRatingShopResponse>?>(null)
    val submitResult: StateFlow<Result<GetRatingShopResponse>?> get() = _submitResult

    fun submitRating(request: RatingShopRequest) {
        viewModelScope.launch {
            val result = followRepository.ratingShop(request)
            _submitResult.value = result
        }
    }

    fun clearResult() {
        _submitResult.value = null
    }
}
