package com.example.greenbuyapp.ui.shop.shopDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.social.model.FollowShopRequest
import com.example.greenbuyapp.data.social.model.FollowShopResponse
import com.example.greenbuyapp.data.social.model.FollowingShop
import com.example.greenbuyapp.data.social.model.UnfollowShopResponse
import com.example.greenbuyapp.domain.social.FollowRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.greenbuyapp.domain.social.FollowStatsRepository
import com.example.greenbuyapp.data.social.model.FollowStatsResponse
import com.example.greenbuyapp.data.social.model.RatingSummaryResponse


class FollowViewModel(
    private val followRepository: FollowRepository,
    private val followStatsRepository: FollowStatsRepository
) : ViewModel() {

    private val _followResult = MutableStateFlow<Result<FollowShopResponse>?>(null)
    val followResult = _followResult.asStateFlow()

    private val _unfollowResult = MutableStateFlow<Result<UnfollowShopResponse>?>(null)
    val unfollowResult = _unfollowResult.asStateFlow()

    private val _followingShops = MutableStateFlow<Result<List<FollowingShop>>?>(null)
    val followingShops = _followingShops.asStateFlow()

    private val _followerCount = MutableStateFlow<Result<Int>?>(null)
    val followerCount = _followerCount.asStateFlow()

    private val _ratingStats = MutableStateFlow<Result<RatingSummaryResponse>>(Result.Loading)
    val ratingStats = _ratingStats.asStateFlow()


    fun follow(shopId: Int) {
        viewModelScope.launch {
            _followResult.value = Result.Loading
            val result = followRepository.followShop(FollowShopRequest(shopId))
            _followResult.value = result
//            loadFollowerCount(shopId)
        }
    }

    fun unfollow(shopId: Int) {
        viewModelScope.launch {
            val req = FollowShopRequest(shopId)
            println("📦 Đang gửi follow với request: $req")
            _unfollowResult.value = Result.Loading
            val result = followRepository.unfollowShop(shopId)
            _unfollowResult.value = result
        }
    }

    fun loadFollowingShops() {
        viewModelScope.launch {
            _followingShops.value = Result.Loading
            val result = followRepository.getFollowingShops()
            _followingShops.value = result
        }
    }

    // đếm follower của shop
    fun loadFollowerCount(shopId: Int) {
        viewModelScope.launch {
            _followerCount.value = Result.Loading
            val result = followRepository.getFollowerCount(shopId)
            _followerCount.value = result
        }
    }

    fun loadShopRatingStats(shopId: Int) {
        viewModelScope.launch {
            // Phải emit Loading trước để trigger lại collect sau đánh giá
            _ratingStats.emit(Result.Loading)

            val result = followRepository.getShopRatingStats(shopId)
            _ratingStats.emit(result)
        }
    }

}
