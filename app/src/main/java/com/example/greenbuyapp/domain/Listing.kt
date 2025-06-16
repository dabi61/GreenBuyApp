package com.example.greenbuyapp.domain

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.example.greenbuyapp.util.NetworkState

data class Listing<T>(
    val pagedList: LiveData<PagedList<T>>,
    val networkState: LiveData<NetworkState>,
    val refresh: () -> Unit,
    val refreshState: LiveData<NetworkState>,
    val retry: () -> Unit
)