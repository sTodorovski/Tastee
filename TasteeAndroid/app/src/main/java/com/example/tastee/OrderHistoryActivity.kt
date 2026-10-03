package com.example.tastee

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tastee.adapter.OrderAdapter
import com.example.tastee.network.RetrofitClient
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var rvOrderHistory: RecyclerView
    private lateinit var chipGroupFilters: ChipGroup
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        rvOrderHistory = findViewById(R.id.rvOrderHistory)
        chipGroupFilters = findViewById(R.id.chipGroupFilters)
        progressBar = findViewById(R.id.progressBar)

        adapter = OrderAdapter(emptyList())

        rvOrderHistory.layoutManager = LinearLayoutManager(this)
        rvOrderHistory.adapter = adapter

        chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val filter = when (checkedIds.first()) {
                R.id.chipToday -> "TODAY"
                R.id.chipWeek -> "THIS_WEEK"
                R.id.chipMonth -> "THIS_MONTH"
                R.id.chipCompleted -> "COMPLETED"
                R.id.chipCancelled -> "CANCELLED"
                else -> null
            }
            fetchOrders(filter)
        }

        fetchOrders(null)
    }

    private fun fetchOrders(filter: String?) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val orders = RetrofitClient.orderApi.getOrderHistory(filter)
                adapter.updateOrders(orders)
            } catch (e: Exception) {
                Toast.makeText(
                    this@OrderHistoryActivity,
                    "Failed to load order history: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}