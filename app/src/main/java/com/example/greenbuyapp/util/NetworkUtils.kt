package com.example.greenbuyapp.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

sealed class NetworkState {
    object EMPTY : NetworkState()
    object LOADING : NetworkState()
    object SUCCESS : NetworkState()
    data class ERROR(val message: String? = null) : NetworkState()
}

sealed class Result<out T> {
    data class Success<out T>(val value: T): Result<T>()
    data class Error(val code: Int? = null, val error: String? = null): Result<Nothing>()
    object Loading: Result<Nothing>()
    object NetworkError: Result<Nothing>()
}

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T
): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> Result.NetworkError
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = throwable.errorBody
                    Result.Error(code, errorResponse)
                }
                else -> Result.Error(null, throwable.message)
            }
        }
    }
}

private val HttpException.errorBody: String?
    get() = try {
        this.response()?.errorBody()?.string()
    } catch (exception: Exception) {
        null
    }

/**
 * Utility functions for multipart requests
 */
object MultipartUtils {
    
    /**
     * Tạo RequestBody từ string cho text fields
     */
    fun createTextPart(text: String): RequestBody {
        return text.toRequestBody("text/plain".toMediaType())
    }
    
    /**
     * Tạo MultipartBody.Part từ File cho image upload
     */
    fun createImagePart(partName: String, file: File): MultipartBody.Part {
        // Xác định media type dựa trên extension
        val mediaType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "image/*"
        }.toMediaType()
        
        println("🖼️ Creating image part:")
        println("   Part name: $partName")
        println("   File name: ${file.name}")
        println("   File size: ${file.length()} bytes")
        println("   File extension: ${file.extension}")
        println("   Media type: $mediaType")
        println("   File exists: ${file.exists()}")
        
        val requestFile = file.asRequestBody(mediaType)
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
    
    /**
     * Tạo MultipartBody.Part từ Uri cho image upload
     */
    fun createImagePart(context: Context, partName: String, uri: Uri): MultipartBody.Part? {
        return try {
            println("📸 Converting URI to file:")
            println("   URI: $uri")
            println("   URI scheme: ${uri.scheme}")
            
            val file = uriToFile(context, uri)
            
            println("✅ File conversion successful:")
            println("   File path: ${file.absolutePath}")
            println("   File size: ${file.length()} bytes")
            println("   File readable: ${file.canRead()}")
            
            createImagePart(partName, file)
        } catch (e: Exception) {
            println("❌ Error creating image part from URI: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Tạo file ảnh dummy 1x1 pixel PNG để gửi khi không có ảnh mới
     */
    fun createDummyImagePart(context: Context, partName: String): MultipartBody.Part {
        val dummyFile = File(context.cacheDir, "dummy_1x1.png")
        
        // Tạo file ảnh dummy 1x1 pixel PNG nếu chưa tồn tại
        if (!dummyFile.exists()) {
            try {
                // Tạo ảnh PNG 1x1 pixel màu trong suốt
                val dummyImageBytes = byteArrayOf(
                    0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(), // PNG signature
                    0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte(),
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0D.toByte(), // IHDR chunk length
                    0x49.toByte(), 0x48.toByte(), 0x44.toByte(), 0x52.toByte(), // IHDR
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(), // width: 1
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(), // height: 1
                    0x08.toByte(), 0x06.toByte(), 0x00.toByte(), 0x00.toByte(), // bit depth, color type, compression, filter
                    0x00.toByte(), // interlace
                    0x37.toByte(), 0x6E.toByte(), 0xF9.toByte(), 0x24.toByte(), // CRC
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0C.toByte(), // IDAT chunk length
                    0x49.toByte(), 0x44.toByte(), 0x41.toByte(), 0x54.toByte(), // IDAT
                    0x78.toByte(), 0x9C.toByte(), 0x62.toByte(), 0x60.toByte(), // zlib header + deflate
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x02.toByte(), 0x00.toByte(), 0x01.toByte(), // deflate data
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), // IDAT CRC
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), // IEND chunk length
                    0x49.toByte(), 0x45.toByte(), 0x4E.toByte(), 0x44.toByte(), // IEND
                    0xAE.toByte(), 0x42.toByte(), 0x60.toByte(), 0x82.toByte()  // IEND CRC
                )
                
                dummyFile.writeBytes(dummyImageBytes)
                println("✅ Created dummy 1x1 PNG file: ${dummyFile.absolutePath}")
            } catch (e: Exception) {
                println("❌ Error creating dummy image file: ${e.message}")
                throw IllegalArgumentException("Cannot create dummy image file: ${e.message}")
            }
        }
        
        return createImagePart(partName, dummyFile)
    }
    
    /**
     * Tạo MultipartBody.Part từ URL ảnh (download ảnh từ URL)
     */
    fun createImagePartFromUrl(context: Context, partName: String, imageUrl: String): MultipartBody.Part? {
        return try {
            // Xử lý URL - nếu là relative path thì thêm base URL
            val fullUrl = if (imageUrl.startsWith("/")) {
                "https://www.utt-school.site$imageUrl"
            } else {
                imageUrl
            }
            
            println("🔄 Processing image URL: $imageUrl -> $fullUrl")
            
            // Tạo URL object
            val url = java.net.URL(fullUrl)
            
            // Tạo file tạm trong cache
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val file = File(context.cacheDir, fileName)
            
            // Download ảnh từ URL
            url.openConnection().getInputStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            println("✅ Downloaded image from URL: $fullUrl to ${file.absolutePath}")
            createImagePart(partName, file)
        } catch (e: Exception) {
            println("❌ Error downloading image from URL: ${e.message}")
            null
        }
    }
    
    /**
     * Convert Uri thành File
     */
    private fun uriToFile(context: Context, uri: Uri): File {
        return try {
            println("🔄 Converting URI to File:")
            println("   URI: $uri")
            println("   Scheme: ${uri.scheme}")
            
            // Try to get file path directly
            when (uri.scheme) {
                "file" -> {
                    // Direct file URI
                    val file = File(uri.path!!)
                    println("   File URI detected: ${file.absolutePath}")
                    file
                }
                "content" -> {
                    println("   Content URI detected, copying to cache...")
                    // Content URI - try to get file name and copy to cache
                    val cursor = context.contentResolver.query(uri, null, null, null, null)
                    cursor?.use {
                        val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                        it.moveToFirst()
                        val originalName = if (nameIndex >= 0) {
                            it.getString(nameIndex)
                        } else null
                        
                        println("   Original file name: $originalName")
                        
                        // Tạo tên file với extension đúng
                        val fileName = when {
                            !originalName.isNullOrEmpty() -> originalName
                            else -> {
                                // Detect mime type để tạo extension đúng
                                val mimeType = context.contentResolver.getType(uri)
                                val extension = when (mimeType) {
                                    "image/jpeg" -> "jpg"
                                    "image/png" -> "png"
                                    "image/gif" -> "gif"
                                    "image/webp" -> "webp"
                                    else -> "jpg" // default to jpg
                                }
                                "avatar_${System.currentTimeMillis()}.$extension"
                            }
                        }
                        
                        println("   Final file name: $fileName")
                        
                        val file = File(context.cacheDir, fileName)
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        
                        println("   Copied to cache: ${file.absolutePath}")
                        println("   File size: ${file.length()} bytes")
                        return file
                    }
                    
                    // Fallback if cursor is null
                    println("   Cursor is null, using fallback...")
                    val mimeType = context.contentResolver.getType(uri)
                    val extension = when (mimeType) {
                        "image/jpeg" -> "jpg"
                        "image/png" -> "png"
                        "image/gif" -> "gif"
                        "image/webp" -> "webp"
                        else -> "jpg"
                    }
                    val fileName = "avatar_${System.currentTimeMillis()}.$extension"
                    val file = File(context.cacheDir, fileName)
                    
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    println("   Fallback copy completed: ${file.absolutePath}")
                    file
                }
                else -> {
                    println("   Unknown scheme, using fallback copy...")
                    // Unknown scheme, try to copy to cache
                    val mimeType = context.contentResolver.getType(uri)
                    val extension = when (mimeType) {
                        "image/jpeg" -> "jpg"
                        "image/png" -> "png"
                        "image/gif" -> "gif"
                        "image/webp" -> "webp"
                        else -> "jpg"
                    }
                    val fileName = "avatar_${System.currentTimeMillis()}.$extension"
                    val file = File(context.cacheDir, fileName)
                    
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    println("   Unknown scheme copy completed: ${file.absolutePath}")
                    file
                }
            }
        } catch (e: Exception) {
            println("❌ Error in uriToFile: ${e.message}")
            e.printStackTrace()
            throw IllegalArgumentException("Cannot convert URI to File: ${e.message}")
        }
    }
}
