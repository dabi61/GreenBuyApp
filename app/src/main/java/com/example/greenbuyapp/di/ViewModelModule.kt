package com.example.greenbuyapp.di


import com.example.greenbuyapp.ui.cart.CartViewModel
import com.example.greenbuyapp.ui.home.HomeViewModel
import com.example.greenbuyapp.ui.login.LoginViewModel
import com.example.greenbuyapp.ui.product.ProductViewModel
import com.example.greenbuyapp.ui.profile.ProfileViewModel
import com.example.greenbuyapp.ui.register.RegisterViewModel
import com.example.greenbuyapp.ui.shop.ShopViewModel
import com.example.greenbuyapp.ui.shop.addProduct.AddProductViewModel
import com.example.greenbuyapp.ui.shop.dashboard.ShopDashboardDetailViewModel
import com.example.greenbuyapp.ui.shop.orderDetail.OrderDetailViewModel
import com.example.greenbuyapp.ui.shop.productManagement.ProductManagementViewModel
import com.example.greenbuyapp.ui.profile.orders.CustomerOrderDetailViewModel
import com.example.greenbuyapp.ui.profile.orders.CustomerOrderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.example.greenbuyapp.ui.notification.NotificationViewModel
import com.example.greenbuyapp.ui.product.trending.TrendingProductViewModel
import com.example.greenbuyapp.ui.shop.shopDetail.FollowViewModel
import com.example.greenbuyapp.ui.social.shopReview.RatingShopViewModel
import com.example.greenbuyapp.ui.social.shopReview.ShopReviewViewModel
import com.example.greenbuyapp.ui.profile.editProfile.address.AddressViewModel
import com.example.greenbuyapp.ui.profile.editProfile.address.AddressAddViewModel
import com.example.greenbuyapp.ui.profile.editProfile.address.AddressUpdateViewModel
import com.example.greenbuyapp.ui.profile.editProfile.infomation.CustomerInformationViewModel
import com.example.greenbuyapp.ui.admin.approve.product.ApproveProductViewModel
import com.example.greenbuyapp.ui.admin.category.CategoryManagementViewModel
import com.example.greenbuyapp.ui.admin.order.AdminOrderDetailViewModel
import com.example.greenbuyapp.ui.admin.order.AdminOrderViewModel
import com.example.greenbuyapp.ui.mall.MallViewModel
import com.example.greenbuyapp.ui.shop.shopDetail.EditShopViewModel

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
    viewModel { ShopViewModel(get(), get()) }
    viewModel { ShopDashboardDetailViewModel(get()) }
    viewModel { OrderDetailViewModel(get()) }
    //Product
    viewModel { ProductManagementViewModel(get()) }
    viewModel { AddProductViewModel(get(), get()) }
    viewModel { TrendingProductViewModel(get()) }

    viewModel { CustomerOrderViewModel(get()) }
    viewModel { CustomerOrderDetailViewModel(get()) }

    viewModel { CartViewModel(get()) }
    //address=
    viewModel { AddressViewModel(get()) }
    viewModel { AddressAddViewModel(get()) }
    viewModel { AddressUpdateViewModel(get()) }
    //information
    viewModel { CustomerInformationViewModel(get()) }

    viewModel { NotificationViewModel(get()) }
    viewModel { FollowViewModel(get(), get()) }
    viewModel { ShopReviewViewModel(get()) }
    viewModel { RatingShopViewModel(get()) }
    viewModel { ApproveProductViewModel(get()) }
    viewModel { CategoryManagementViewModel(get()) }
    viewModel { AdminOrderViewModel(get()) }
    viewModel { AdminOrderDetailViewModel(get()) }
    viewModel { EditShopViewModel(get()) }
    viewModel { MallViewModel(get(), get(), get(), get()) }


//    viewModel { SettingsViewModel(androidContext()) }
//    viewModel { AutoWallpaperSettingsViewModel(get()) }
//    viewModel { AutoWallpaperHistoryViewModel(get()) }
//    viewModel { AutoWallpaperCollectionViewModel(get(), get(), get()) }
//    viewModel { UpgradeViewModel(get(), get()) }
//    viewModel { DonationViewModel(get(), get()) }
//    viewModel { MuzeiSettingsViewModel(get()) }
}