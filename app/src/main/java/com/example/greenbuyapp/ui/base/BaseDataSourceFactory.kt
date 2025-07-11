package com.example.greenbuyapp.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.Config
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import com.example.greenbuyapp.di.Properties
import com.example.greenbuyapp.domain.Listing

abstract class BaseDataSourceFactory<T> : DataSource.Factory<Int, T>() {

    private val sourceLiveData = MutableLiveData<BaseDataSource<T>>()

    private val config = Config(
        pageSize = Properties.DEFAULT_PAGE_SIZE,
        initialLoadSizeHint = Properties.DEFAULT_PAGE_SIZE,
        prefetchDistance = Properties.DEFAULT_PAGE_SIZE / 2,
        enablePlaceholders = false
    )

    abstract fun createDataSource(): BaseDataSource<T>

    override fun create(): DataSource<Int, T> {
        val source = createDataSource()
        sourceLiveData.postValue(source)
        return source
    }

    fun createListing() = Listing<T>(
        pagedList = LivePagedListBuilder(this, config).build(),
        networkState = this.sourceLiveData.switchMap(
            BaseDataSource<T>::networkState
        ),
        refresh = { this.sourceLiveData.value?.invalidate() },
        refreshState = this.sourceLiveData.switchMap(
            BaseDataSource<T>::initialLoadState
        ),
        retry = {}
    )
}
