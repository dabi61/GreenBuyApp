package com.example.greenbuyapp.di


import com.example.greenbuyapp.domain.login.LoginRepository
import com.example.greenbuyapp.domain.register.RegisterRepository
import com.example.greenbuyapp.domain.user.UserRepository
import org.koin.dsl.module

val repositoryModule = module {

//    single(createdAtStart = true) { PhotoRepository(get(), get(), get(), get()) }
//    single(createdAtStart = true) { CollectionRepository(get(), get(), get()) }
    single(createdAtStart = true) { UserRepository(get(), get()) }
    single(createdAtStart = true) { LoginRepository(get(), get(), get(), get()) }
    single(createdAtStart = true) { RegisterRepository(get()) }


//    single(createdAtStart = true) { BillingRepository(androidApplication()) }
//
//    single { AutoWallpaperRepository(get(), get()) }
}