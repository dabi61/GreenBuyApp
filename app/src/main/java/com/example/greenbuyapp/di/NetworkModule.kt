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
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import com.example.greenbuyapp.data.notice.NoticeService
import android.content.Context

private const val CONTENT_TYPE = "Content-Type"
private const val APPLICATION_JSON = "application/json"
private const val ACCEPT_VERSION = "Accept-Version"

private const val BASE_URL = "https://www.utt-school.site/"
private const val API_BASE_URL = "https://www.utt-school.site/"

val networkModule = module {

    single(createdAtStart = true) { createOkHttpClient(get(), androidContext()) }
    single(createdAtStart = true) { AccessTokenProvider(androidContext()) }
    single(createdAtStart = true) { TokenExpiredManager() }
    factory { createAccessTokenInterceptor(get(), get()) }
    factory { createConverterFactory() }
    factory { createService<UserService>(get(), get()) }
    factory { createService<SearchService>(get(), get()) }
    factory { createService<AuthorizationService>(get(), get(), BASE_URL) }
    factory { createService<RegisterService>(get(), get()) }
    factory { createService<ProductService>(get(), get()) }
    factory { createService<CategoryService>(get(), get()) }
    factory { createService<SocialService>(get(), get(), BASE_URL) }
    factory { createService<ShopService>(get(), get()) }
    factory { createService<CartService>(get(), get()) }
    factory { createService<NoticeService>(get(), get()) }
}

private fun createOkHttpClient(
    accessTokenInterceptor: AccessTokenInterceptor,
    context: Context
): OkHttpClient {
    return OkHttpClient.Builder()
        .addNetworkInterceptor(createHeaderInterceptor())
        .addInterceptor(createHttpLoggingInterceptor())
        .addInterceptor(accessTokenInterceptor)
        .addInterceptor(createCacheInterceptor())
        .cache(Cache(context.cacheDir, 50 * 1024 * 1024))
        // ✅ Tối ưu timeout cho upload ảnh
        .connectTimeout(30, TimeUnit.SECONDS)  // Tăng từ 10s lên 30s
        .readTimeout(60, TimeUnit.SECONDS)     // Tăng từ 15s lên 60s  
        .writeTimeout(60, TimeUnit.SECONDS)    // Tăng từ 10s lên 60s
        // ✅ Thêm connection pool để tái sử dụng connection
        .connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
        // ✅ Thêm retry interceptor cho upload
        .addInterceptor(createRetryInterceptor())
        // ✅ Thêm progress interceptor cho upload
        .addInterceptor(createProgressInterceptor())
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

private fun createCacheInterceptor(): Interceptor {
    return Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)
        
        // Cache GET requests for 5 minutes
        if (request.method == "GET") {
            response.newBuilder()
                .header("Cache-Control", "public, max-age=300")
                .build()
        } else {
            response
        }
    }
}

/**
 * ✅ Retry interceptor cho upload ảnh
 */
private fun createRetryInterceptor(): Interceptor {
    return Interceptor { chain ->
        val request = chain.request()
        var response = chain.proceed(request)
        var retryCount = 0
        val maxRetries = 3
        
        // ✅ Chỉ retry cho POST/PUT requests (upload)
        while (!response.isSuccessful && retryCount < maxRetries && 
               (request.method == "POST" || request.method == "PUT")) {
            retryCount++
            println("🔄 Retry attempt $retryCount for ${request.method} ${request.url}")
            
            // Đóng response cũ
            response.close()
            
            // Chờ một chút trước khi retry
            Thread.sleep(1000L * retryCount)
            
            // Thử lại
            response = chain.proceed(request)
        }
        
        response
    }
}

/**
 * ✅ Progress interceptor để theo dõi tiến trình upload
 */
private fun createProgressInterceptor(): Interceptor {
    return Interceptor { chain ->
        val request = chain.request()
        
        // ✅ Chỉ track progress cho upload requests
        if (request.method == "POST" || request.method == "PUT") {
            println("📤 Starting upload: ${request.method} ${request.url}")
            val startTime = System.currentTimeMillis()
            
            val response = chain.proceed(request)
            
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            if (response.isSuccessful) {
                println("✅ Upload completed in ${duration}ms")
            } else {
                println("❌ Upload failed in ${duration}ms: ${response.code}")
            }
            
            response
        } else {
            chain.proceed(request)
        }
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
    baseUrl: String = API_BASE_URL
): T {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(converterFactory)
        .build()
        .create(T::class.java)
}

object Properties {

    const val DEFAULT_PAGE_SIZE = 30
}
