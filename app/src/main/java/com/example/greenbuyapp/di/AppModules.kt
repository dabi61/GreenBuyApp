package com.example.greenbuyapp.di

import com.b_lam.resplash.di.storageModule

val appModules = listOf(
    networkModule,
    viewModelModule,
    repositoryModule,
    storageModule,
    managerModule,
    workerModule
)
