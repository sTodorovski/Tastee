package com.example.tastee.api

import com.example.tastee.dto.UpdateUserRequest
import com.example.tastee.dto.UserDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part

interface UserApi {
    @GET("api/users/me")
    suspend fun getCurrentUser(): UserDto

    @PUT("api/users/me")
    suspend fun updateCurrentUser(@Body request: UpdateUserRequest): UserDto

    @Multipart
    @PUT("api/users/me/profile-picture")
    suspend fun updateProfilePicture(@Part file: MultipartBody.Part): UserDto
}