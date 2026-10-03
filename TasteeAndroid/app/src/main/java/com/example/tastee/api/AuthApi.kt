package com.example.tastee.api

import com.example.tastee.dto.LoginRequest
import com.example.tastee.dto.LoginResponse
import com.example.tastee.dto.RegistrationRequest
import com.example.tastee.dto.RegistrationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body request: RegistrationRequest): RegistrationResponse

    @POST("logout")
    suspend fun logout(): Response<Unit>
}