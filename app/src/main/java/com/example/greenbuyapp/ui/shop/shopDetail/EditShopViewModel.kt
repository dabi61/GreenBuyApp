package com.example.greenbuyapp.ui.shop.shopDetail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenbuyapp.data.shop.model.UpdateShopResponse
import com.example.greenbuyapp.domain.shop.ShopRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditShopViewModel(
    private val shopRepository: ShopRepository,
) : ViewModel() {

    private val _updateResult = MutableStateFlow<Result<UpdateShopResponse>?>(null)
    val updateResult: StateFlow<Result<UpdateShopResponse>?> = _updateResult

    fun updateShop(
        context: Context,
        name: String,
        phoneNumber: String,
        isActive: Boolean = true,
        isOnline: Boolean = true,
        avatarUri: Uri?
    ) {
        viewModelScope.launch {
            val result = shopRepository.updateMyShop(
                context = context,
                name = name,
                phoneNumber = phoneNumber,
                isActive = isActive,
                isOnline = isOnline,
                avatarUri = avatarUri
            )
            _updateResult.value = result
        }
    }
}
