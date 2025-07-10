package com.example.greenbuyapp.di


import com.example.greenbuyapp.domain.cart.CartRepository
import com.example.greenbuyapp.domain.category.CategoryRepository
import com.example.greenbuyapp.domain.login.LoginRepository
import com.example.greenbuyapp.domain.product.ProductRepository
import com.example.greenbuyapp.domain.register.RegisterRepository
import com.example.greenbuyapp.domain.shop.ShopRepository
import com.example.greenbuyapp.domain.social.FollowStatsRepository
import com.example.greenbuyapp.domain.user.UserRepository
import org.koin.dsl.module
import com.example.greenbuyapp.domain.notice.NoticeRepository
import com.example.greenbuyapp.domain.social.FollowRepository

val repositoryModule = module {

//    single(createdAtStart = true) { PhotoRepository(get(), get(), get(), get()) }
//    single(createdAtStart = true) { CollectionRepository(get(), get(), get()) }
    single(createdAtStart = true) { UserRepository(get(), get()) }
    single(createdAtStart = true) { LoginRepository(get(), get(), get()) }
    single(createdAtStart = true) { RegisterRepository(get()) }
    single(createdAtStart = true) { ProductRepository(get()) }
    single(createdAtStart = true) { CategoryRepository(get()) }
    single(createdAtStart = true) { FollowStatsRepository(get()) }
    single(createdAtStart = true) { ShopRepository(get(), get()) }
    single(createdAtStart = true) { CartRepository(get()) }
    single(createdAtStart = true) { NoticeRepository(get()) }
    single(createdAtStart = true) { FollowRepository(get()) }

//    single(createdAtStart = true) { BillingRepository(androidApplication()) }
//
//    single { AutoWallpaperRepository(get(), get()) }
}