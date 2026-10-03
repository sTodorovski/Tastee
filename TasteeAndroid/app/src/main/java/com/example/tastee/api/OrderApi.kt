package com.example.tastee.api

import com.example.tastee.dto.CreateOrderRequest
import com.example.tastee.dto.CreateOrderResponse
import com.example.tastee.dto.OrderResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApi {
    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): CreateOrderResponse

    @GET("orders/my-orders")
    suspend fun getMyOrders(): List<OrderResponse>

    @GET("orders/available")
    suspend fun getAvailableOrders(): List<OrderResponse>

    @POST("orders/{orderId}/accept")
    suspend fun acceptOrder(@Path("orderId") orderId: Long): OrderResponse

    @GET("orders/restaurant-orders")
    suspend fun getRestaurantOrders(): List<OrderResponse>

    @GET("orders/owner-history")
    suspend fun getOrderHistory(@Query("filter") filter: String? = null): List<OrderResponse>

    @GET("orders/{orderId}")
    suspend fun getOrderById(@Path("orderId") orderId: Long): OrderResponse

    @GET("orders/delivery-orders")
    suspend fun getDeliveryDriverOrders(): List<OrderResponse>

    @POST("orders/{orderId}/finish")
    suspend fun finishDelivery(@Path("orderId") orderId: Long): OrderResponse

    @POST("orders/{orderId}/cancel-delivery")
    suspend fun cancelDelivery(@Path("orderId") orderId: Long): OrderResponse
}