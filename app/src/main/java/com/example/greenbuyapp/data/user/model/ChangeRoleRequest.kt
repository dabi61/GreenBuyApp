package com.example.greenbuyapp.data.user.model

import com.squareup.moshi.JsonClass
 
@JsonClass(generateAdapter = true)
data class ChangeRoleRequest(
    val new_role: String
) 