package com.example.tastee.dto

data class AnalyticsResponse(
    val totalOrders: Int,
    val totalRevenue: Double,
    val averageOrderValue: Double,
    val completedOrders: Int,
    val cancelledOrders: Int,
    val topDishes: List<DishStat>,
    val chartData: List<ChartPoint>
)

data class DishStat(
    val name: String,
    val count: Int
)

data class ChartPoint(
    val label: String,
    val revenue: Double
)