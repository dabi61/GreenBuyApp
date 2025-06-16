package com.example.greenbuyapp.data.collection.model

import android.os.Parcelable
import com.example.greenbuyapp.data.collection.model.Collection
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class CollectionPhotoResult(
//    val photo: Photo?,
    val collection: Collection?
) : Parcelable