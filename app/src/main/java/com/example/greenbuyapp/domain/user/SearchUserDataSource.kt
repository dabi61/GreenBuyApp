package com.example.greenbuyapp.domain.user

import com.example.greenbuyapp.data.search.SearchService
import com.example.greenbuyapp.data.user.model.User
import com.example.greenbuyapp.ui.base.BaseDataSource
import kotlinx.coroutines.CoroutineScope

class SearchUserDataSource(
    private val searchService: SearchService,
    private val query: String,
    scope: CoroutineScope
) : BaseDataSource<User>(scope) {

    override suspend fun getPage(page: Int, perPage: Int): List<User> {
        return searchService.searchUsers(
            query = query,
            page = page,
            per_page = perPage
        ).results
    }
}