package com.example.tastee.network

import com.example.tastee.dto.OrderResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApiService {
    @GET("orders/owner-history")
    suspend fun getOrderHistory(@Query("filter") filter: String? = null): List<OrderResponse>

    @GET("orders/{orderId}")
    suspend fun getOrderById(@Path("orderId") orderId: Long): OrderResponse
}