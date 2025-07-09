package com.example.greenbuyapp.ui.social.shopReview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.social.model.GetRatingShopResponse
import com.example.greenbuyapp.domain.social.FollowRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.launch

class ShopReviewViewModel(
    private val followRepository: FollowRepository
) : ViewModel() {
    private val _reviews = MutableLiveData<Result<List<GetRatingShopResponse>>>()
    val reviews: LiveData<Result<List<GetRatingShopResponse>>> = _reviews

    fun loadShopRatings(shopId: Int, page: Int? = null, limit: Int? = null) {
        viewModelScope.launch {
            _reviews.value = Result.Loading
            _reviews.value = followRepository.getShopRatings(shopId, page, limit)
        }
    }
}