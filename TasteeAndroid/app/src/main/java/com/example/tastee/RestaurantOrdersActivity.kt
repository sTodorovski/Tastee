package com.example.tastee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tastee.adapter.RestaurantOrderAdapter
import com.example.tastee.dto.OrderResponse
import com.example.tastee.network.RetrofitClient
import kotlinx.coroutines.launch

class RestaurantOrdersActivity : AppCompatActivity() {

    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var ordersProgressBar: ProgressBar
    private lateinit var emptyOrdersText: TextView

    private lateinit var adapter: RestaurantOrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_orders)

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView)
        ordersProgressBar = findViewById(R.id.ordersProgressBar)
        emptyOrdersText = findViewById(R.id.emptyOrdersText)

        adapter = RestaurantOrderAdapter(emptyList()) { order ->
            openOrderDetails(order)
        }

        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersRecyclerView.adapter = adapter

        loadOrders()
    }

    private fun openOrderDetails(order: OrderResponse) {
        val intent = Intent(
            this,
            RestaurantOrderDetailsActivity::class.java
        ).apply {
            putExtra("ORDER_ID", order.id)
        }

        startActivity(intent)
    }

    private fun loadOrders() {
        ordersProgressBar.visibility = View.VISIBLE
        emptyOrdersText.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val orders = RetrofitClient.orderApi.getRestaurantOrders()

                adapter.updateOrders(orders)

                if (orders.isEmpty()) {
                    emptyOrdersText.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@RestaurantOrdersActivity,
                    e.message ?: "Failed to load orders",
                    Toast.LENGTH_LONG
                ).show()

            } finally {
                ordersProgressBar.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {
            loadOrders()
        }
    }
}