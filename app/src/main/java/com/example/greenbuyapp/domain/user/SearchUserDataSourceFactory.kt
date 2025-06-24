package com.example.greenbuyapp.domain.user

import com.example.greenbuyapp.data.search.SearchService
import com.example.greenbuyapp.data.user.model.User
import com.example.greenbuyapp.ui.base.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class SearchUserDataSourceFactory(
    private val searchService: SearchService,
    private val query: String,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<User>() {

    override fun createDataSource() = SearchUserDataSource(searchService, query, scope)
}