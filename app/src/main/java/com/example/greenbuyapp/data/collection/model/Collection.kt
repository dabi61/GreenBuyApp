package com.example.greenbuyapp.data.collection.model

import android.nfc.Tag
import android.os.Parcelable
import com.example.greenbuyapp.data.user.model.User
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Collection(
    val id: String,
    val title: String,
    val description: String?,
    val published_at: String?,
    val updated_at: String?,
    val curated: Boolean?,
    val featured: Boolean?,
    val total_photos: Int,
    val private: Boolean?,
    val share_key: String?,
    val tags: List<Tag>?,
//    val cover_photo: Photo?,
//    val preview_photos: List<Photo>?,
    val user: User?,
    val links: Links?
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Links(
    val self: String?,
    val html: String?,
    val photos: String?
) : Parcelable
