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
        val requestFile = file.asRequestBody("image/*".toMediaType())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
    
    /**
     * Tạo MultipartBody.Part từ Uri cho image upload
     */
    fun createImagePart(context: Context, partName: String, uri: Uri): MultipartBody.Part? {
        return try {
            val file = uriToFile(context, uri)
            createImagePart(partName, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Convert Uri thành File
     */
    private fun uriToFile(context: Context, uri: Uri): File {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            it.moveToFirst()
            val name = it.getString(nameIndex)
            
            val file = File(context.cacheDir, name)
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return file
        }
        throw IllegalArgumentException("Cannot convert URI to File")
    }
}
