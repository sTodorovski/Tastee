package com.example.tastee.api

import com.example.tastee.dto.Dish
import com.example.tastee.dto.DishAvailabilityRequest
import com.example.tastee.dto.DishDiscountRequest
import com.example.tastee.dto.DishRequest
import com.example.tastee.dto.ImageUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface DishApi {
    @GET("api/dishes")
    suspend fun getAllDishes(): List<Dish>

    @GET("api/dishes/{id}")
    suspend fun getDish(@Path("id") id: Long): Dish

    @POST("api/dishes")
    suspend fun createDish(@Body request: DishRequest): Response<Dish>

    @PUT("api/dishes/{id}")
    suspend fun updateDish(@Path("id") id: Long, @Body dish: Dish): Response<Dish>

    @DELETE("api/dishes/{id}")
    suspend fun deleteDish(@Path("id") id: Long): Response<Unit>

    @GET("api/dishes/restaurant/{restaurantId}")
    suspend fun getRestaurantDishes(@Path("restaurantId") restaurantId: Long): List<Dish>

    @PATCH("api/dishes/{id}/availability")
    suspend fun updateAvailability(@Path("id") id: Long, @Body request: DishAvailabilityRequest): Response<Dish>

    @PATCH("api/dishes/{id}/discount")
    suspend fun updateDiscount(@Path("id") id: Long, @Body request: DishDiscountRequest): Response<Dish>

    @Multipart
    @POST("restaurants/upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<ImageUploadResponse>
}