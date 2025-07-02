package com.example.greenbuyapp.di


import com.example.greenbuyapp.data.authorization.AuthorizationService
import com.example.greenbuyapp.data.cart.CartService
import com.example.greenbuyapp.data.product.ProductService
import com.example.greenbuyapp.data.register.RegisterService
import com.example.greenbuyapp.data.search.SearchService
import com.example.greenbuyapp.data.user.UserService
import com.example.greenbuyapp.domain.login.AccessTokenInterceptor
import com.example.greenbuyapp.domain.login.AccessTokenProvider
import com.example.greenbuyapp.domain.login.TokenExpiredManager
import com.example.greenbuyapp.data.category.CategoryService
import com.example.greenbuyapp.data.shop.ShopService
import com.example.greenbuyapp.data.social.SocialService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private const val CONTENT_TYPE = "Content-Type"
private const val APPLICATION_JSON = "application/json"
private const val ACCEPT_VERSION = "Accept-Version"

private const val UNSPLASH_BASE_URL = "https://www.utt-school.site/"
private const val UNSPLASH_API_BASE_URL = "https://www.utt-school.site/"

val networkModule = module {

    single(createdAtStart = true) { createOkHttpClient(get()) }
    single(createdAtStart = true) { AccessTokenProvider(androidContext()) }
    single(createdAtStart = true) { TokenExpiredManager() }
    factory { createAccessTokenInterceptor(get(), get()) }
    factory { createConverterFactory() }
    factory { createService<UserService>(get(), get()) }
    factory { createService<SearchService>(get(), get()) }
    factory { createService<AuthorizationService>(get(), get(), UNSPLASH_BASE_URL) }
    factory { createService<RegisterService>(get(), get()) }
    factory { createService<ProductService>(get(), get()) }
    factory { createService<CategoryService>(get(), get()) }
    factory { createService<SocialService>(get(), get(), UNSPLASH_BASE_URL) }
    factory { createService<ShopService>(get(), get()) }
    factory { createService<CartService>(get(), get()) }
}

private fun createOkHttpClient(accessTokenInterceptor: AccessTokenInterceptor): OkHttpClient {
    return OkHttpClient.Builder()
        .addNetworkInterceptor(createHeaderInterceptor())
        .addInterceptor(createHttpLoggingInterceptor())
        .addInterceptor(accessTokenInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}

private fun createHeaderInterceptor(): Interceptor {
    return Interceptor { chain ->
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .apply {
                // Only add Content-Type if not already present (to allow @FormUrlEncoded)
                if (originalRequest.header(CONTENT_TYPE) == null) {
                    addHeader(CONTENT_TYPE, APPLICATION_JSON)
                }
            }
            .addHeader(ACCEPT_VERSION, "v1")
            .build()
        chain.proceed(newRequest)
    }
}

private fun createHttpLoggingInterceptor(): Interceptor {
    return HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
        redactHeader("Authorization")
    }
}

private fun createAccessTokenInterceptor(
    accessTokenProvider: AccessTokenProvider,
    tokenExpiredManager: TokenExpiredManager
): AccessTokenInterceptor {
    return AccessTokenInterceptor(accessTokenProvider, tokenExpiredManager)
}

private fun createConverterFactory(): MoshiConverterFactory {
    return MoshiConverterFactory.create()
}

private inline fun <reified T> createService(
    okHttpClient: OkHttpClient,
    converterFactory: MoshiConverterFactory,
    baseUrl: String = UNSPLASH_API_BASE_URL
): T {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(converterFactory)
        .build()
        .create(T::class.java)
}
//
//private fun createDownloadService(
//    accessTokenInterceptor: AccessTokenInterceptor,
//    converterFactory: MoshiConverterFactory
//): DownloadService {
//    val okHttpClient = OkHttpClient.Builder()
//        .addNetworkInterceptor(createHeaderInterceptor())
//        .addInterceptor(createHttpLoggingInterceptor())
//        .addInterceptor(accessTokenInterceptor)
//        .connectTimeout(20, TimeUnit.SECONDS)
//        .readTimeout(120, TimeUnit.SECONDS)
//        .writeTimeout(120, TimeUnit.SECONDS)
//        .build()
//    return Retrofit.Builder()
//        .baseUrl(UNSPLASH_API_BASE_URL)
//        .client(okHttpClient)
//        .addConverterFactory(converterFactory)
//        .build()
//        .create(DownloadService::class.java)
//}

object Properties {

    const val DEFAULT_PAGE_SIZE = 30
}
