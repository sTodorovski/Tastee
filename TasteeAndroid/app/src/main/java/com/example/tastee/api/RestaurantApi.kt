package com.example.tastee.api

import com.example.tastee.dto.CreateReviewRequest
import com.example.tastee.dto.Dish
import com.example.tastee.dto.ImageUploadResponse
import com.example.tastee.dto.Restaurant
import com.example.tastee.dto.RestaurantOrderStatusResponse
import com.example.tastee.dto.RestaurantRatingSummaryDto
import com.example.tastee.dto.RestaurantRequest
import com.example.tastee.dto.RestaurantStatusRequest
import com.example.tastee.dto.RestaurantUpdateRequest
import com.example.tastee.dto.RestaurantWorkingHour
import com.example.tastee.dto.RestaurantWorkingHourRequest
import com.example.tastee.dto.ReviewDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RestaurantApi {
    @GET("restaurants")
    suspend fun getRestaurants(): List<Restaurant>

    @GET("restaurants/{id}")
    suspend fun getRestaurant(@Path("id") id: Long): Restaurant

    @GET("restaurants/search")
    suspend fun searchRestaurants(@Query("name") name: String): List<Restaurant>

    @GET("restaurants/mine")
    suspend fun getMyRestaurant(): Restaurant

    @POST("restaurants")
    suspend fun createRestaurant(@Body request: RestaurantRequest): Restaurant

    @PUT("restaurants/mine")
    suspend fun updateMyRestaurant(@Body request: RestaurantUpdateRequest): Restaurant

    @PATCH("restaurants/mine/status")
    suspend fun updateMyRestaurantStatus(@Body request: RestaurantStatusRequest): Restaurant

    @GET("restaurants/{id}/order-status")
    suspend fun getRestaurantOrderStatus(@Path("id") restaurantId: Long): RestaurantOrderStatusResponse

    @GET("restaurants/{id}/dishes")
    suspend fun getRestaurantDishes(@Path("id") restaurantId: Long): List<Dish>

    @GET("api/dishes")
    suspend fun getAllDishes(): List<Dish>

    @GET("restaurants/{id}/reviews")
    suspend fun getRestaurantReviews(@Path("id") restaurantId: Long): List<ReviewDto>

    @POST("restaurants/{id}/reviews")
    suspend fun addReview(@Path("id") restaurantId: Long, @Body request: CreateReviewRequest): ReviewDto

    @GET("restaurants/mine/working-hours")
    suspend fun getMyWorkingHours(): List<RestaurantWorkingHour>

    @PUT("restaurants/mine/working-hours")
    suspend fun updateMyWorkingHours(@Body requests: List<RestaurantWorkingHourRequest>): List<RestaurantWorkingHour>

    @GET("restaurants/{id}/reviews/summary")
    suspend fun getRestaurantReviewSummary(@Path("id") restaurantId: Long): RestaurantRatingSummaryDto

    @Multipart
    @POST("restaurants/upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): ImageUploadResponse
}