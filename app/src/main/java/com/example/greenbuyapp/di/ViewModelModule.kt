package com.example.greenbuyapp.di


import com.example.greenbuyapp.ui.home.HomeViewModel
import com.example.greenbuyapp.ui.login.LoginViewModel
import com.example.greenbuyapp.ui.product.ProductViewModel
import com.example.greenbuyapp.ui.profile.ProfileViewModel
import com.example.greenbuyapp.ui.register.RegisterViewModel
import com.example.greenbuyapp.ui.shop.ShopViewModel
import com.example.greenbuyapp.ui.shop.dashboard.ShopDashboardDetailViewModel
import com.example.greenbuyapp.ui.shop.orderDetail.OrderDetailViewModel
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
    viewModel { HomeViewModel(get(), get()) }
    viewModel { ProductViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { ShopViewModel(get()) }
    viewModel { ShopDashboardDetailViewModel(get()) }
    viewModel { OrderDetailViewModel(get()) }

//    viewModel { SettingsViewModel(androidContext()) }
//    viewModel { AutoWallpaperSettingsViewModel(get()) }
//    viewModel { AutoWallpaperHistoryViewModel(get()) }
//    viewModel { AutoWallpaperCollectionViewModel(get(), get(), get()) }
//    viewModel { UpgradeViewModel(get(), get()) }
//    viewModel { DonationViewModel(get(), get()) }
//    viewModel { MuzeiSettingsViewModel(get()) }
}