package com.example.tastee

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tastee.dto.OrderResponse
import com.example.tastee.network.RetrofitClient
import kotlinx.coroutines.launch

class RestaurantOrderDetailsActivity : AppCompatActivity() {

    private lateinit var orderNumberText: TextView
    private lateinit var orderStatusText: TextView
    private lateinit var orderTimeText: TextView
    private lateinit var orderItemsText: TextView
    private lateinit var orderAddressText: TextView
    private lateinit var orderExpressText: TextView
    private lateinit var orderPaymentText: TextView
    private lateinit var orderTipText: TextView
    private lateinit var orderTotalText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_order_details)

        orderNumberText = findViewById(R.id.orderNumberText)
        orderStatusText = findViewById(R.id.orderStatusText)
        orderTimeText = findViewById(R.id.orderTimeText)
        orderItemsText = findViewById(R.id.orderItemsText)
        orderAddressText = findViewById(R.id.orderAddressText)
        orderExpressText = findViewById(R.id.orderExpressText)
        orderPaymentText = findViewById(R.id.orderPaymentText)
        orderTipText = findViewById(R.id.orderTipText)
        orderTotalText = findViewById(R.id.orderTotalText)
        progressBar = findViewById(R.id.orderDetailsProgressBar)

        val orderId = intent.getLongExtra("ORDER_ID", -1L)

        if (orderId == -1L) {
            Toast.makeText(
                this,
                "Invalid order",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        loadOrder(orderId)
    }

    private fun loadOrder(orderId: Long) {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val order = RetrofitClient.orderApi.getOrderById(orderId)
                displayOrder(order)
            } catch (e: Exception) {
                Toast.makeText(
                    this@RestaurantOrderDetailsActivity,
                    e.message ?: "Failed to load order",
                    Toast.LENGTH_LONG
                ).show()

                finish()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun displayOrder(order: OrderResponse) {
        orderNumberText.text = "#${order.id}"
        orderStatusText.text = order.status
        orderTimeText.text = order.createdAt

        orderItemsText.text = order.items.joinToString("\n") {
            "${it.quantity}x ${it.dishName} — $${"%.2f".format(it.price)}"
        }

        orderAddressText.text = if (order.deliveryAddress.isBlank()) {
            "No delivery address"
        } else {
            order.deliveryAddress
        }

        if (order.expressDelivery) {
            orderExpressText.visibility = View.VISIBLE
            orderExpressText.text = "EXPRESS DELIVERY"
        } else {
            orderExpressText.visibility = View.GONE
        }

        orderPaymentText.text = "Payment: ${order.paymentMethod}"
        orderTipText.text = "Tip: $${"%.2f".format(order.tip)}"
        orderTotalText.text = "$${"%.2f".format(order.total)}"
    }
}