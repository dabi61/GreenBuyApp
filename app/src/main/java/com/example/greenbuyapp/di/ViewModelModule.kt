package com.example.greenbuyapp.di


import com.example.greenbuyapp.ui.login.LoginViewModel
import com.example.greenbuyapp.ui.register.RegisterViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

//    viewModel { MainViewModel(get(), get(), get(), get()) }
//    viewModel { PhotoDetailViewModel(get(), get(), get()) }
//    viewModel { CollectionDetailViewModel(get(), get(), get(), get()) }
//    viewModel { SearchViewModel(get(), get(), get()) }
//    viewModel { UserViewModel(get(), get(), get(), get()) }
//    viewModel { EditProfileViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get())}
//    viewModel { SettingsViewModel(androidContext()) }
//    viewModel { AutoWallpaperSettingsViewModel(get()) }
//    viewModel { AutoWallpaperHistoryViewModel(get()) }
//    viewModel { AutoWallpaperCollectionViewModel(get(), get(), get()) }
//    viewModel { UpgradeViewModel(get(), get()) }
//    viewModel { DonationViewModel(get(), get()) }
//    viewModel { MuzeiSettingsViewModel(get()) }
}