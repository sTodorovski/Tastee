package com.example.tastee

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tastee.network.RetrofitClient
import com.example.tastee.utils.DeliveryNotificationHelper
import kotlinx.coroutines.launch

class DeliveryOrderDetailsActivity : AppCompatActivity() {
    private var orderId: Long = -1L
    private var currentOrderStatus: String = ""
    private var isDriver: Boolean = false

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                executeAcceptOrder()
            } else {
                Toast.makeText(this, "Notification permission denied. Order accepted without notification.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_order_details)

        orderId = intent.getLongExtra("ORDER_ID", -1L)
        isDriver = intent.getBooleanExtra("IS_DRIVER", false)

        if (orderId == -1L) {
            Toast.makeText(this, "Invalid order.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (!isDriver) {
            findViewById<TextView>(R.id.orderId).visibility = View.GONE
            findViewById<Button>(R.id.acceptButton).visibility = View.GONE
            findViewById<Button>(R.id.declineButton).visibility = View.GONE
        }

        loadOrder()

        findViewById<Button>(R.id.acceptButton).setOnClickListener {
            when (currentOrderStatus) {
                "PAID" -> acceptOrder()
                "DELIVERING" -> finishDelivery()
            }
        }

        findViewById<Button>(R.id.declineButton).setOnClickListener {
            when (currentOrderStatus) {
                "PAID" -> finish()
                "DELIVERING" -> cancelDelivery()
            }
        }
    }

    private fun loadOrder() {
        lifecycleScope.launch {
            try {
                val order = RetrofitClient.orderApi.getOrderById(orderId)
                currentOrderStatus = order.status.toString()

                findViewById<TextView>(R.id.restaurantName).text = order.restaurantName

                val orderIdTextView = findViewById<TextView>(R.id.orderId)
                if (isDriver) {
                    orderIdTextView.text = "Order #${order.id}"
                    orderIdTextView.visibility = View.VISIBLE
                } else {
                    orderIdTextView.visibility = View.GONE
                }

                findViewById<TextView>(R.id.deliveryAddress).text = "Delivery address: ${order.deliveryAddress}"
                findViewById<TextView>(R.id.orderItems).text = order.items.joinToString("\n") { item ->
                    "${item.dishName} x${item.quantity} — €%.2f".format(item.price * item.quantity)
                }
                findViewById<TextView>(R.id.orderTotal).text = "Total: €%.2f".format(order.total)
                findViewById<TextView>(R.id.orderTip).text = "Tip: €%.2f".format(order.tip)
                findViewById<TextView>(R.id.orderStatus).text = order.status.toString()

                val logoImageView = findViewById<ImageView>(R.id.restaurantLogo)
                if (!order.restaurantImageUrl.isNullOrBlank()) {
                    val imageUrl = if (order.restaurantImageUrl.startsWith("http")) {
                        order.restaurantImageUrl
                    } else {
                        "http://10.0.2.2:8080${order.restaurantImageUrl}"
                    }

                    Glide.with(this@DeliveryOrderDetailsActivity)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(logoImageView)
                } else {
                    logoImageView.setImageResource(R.drawable.ic_person)
                }

                val acceptButton = findViewById<Button>(R.id.acceptButton)
                val declineButton = findViewById<Button>(R.id.declineButton)

                if (isDriver) {
                    when (currentOrderStatus) {
                        "PAID" -> {
                            acceptButton.visibility = View.VISIBLE
                            declineButton.visibility = View.VISIBLE
                            acceptButton.text = "ACCEPT"
                            declineButton.text = "DECLINE"
                        }
                        "DELIVERING" -> {
                            acceptButton.visibility = View.VISIBLE
                            declineButton.visibility = View.VISIBLE
                            acceptButton.text = "FINISH DELIVERY"
                            declineButton.text = "CANCEL DELIVERY"
                        }
                        else -> {
                            acceptButton.visibility = View.GONE
                            declineButton.visibility = View.GONE
                        }
                    }
                } else {
                    acceptButton.visibility = View.GONE
                    declineButton.visibility = View.GONE
                }

            } catch (e: Exception) {
                Toast.makeText(this@DeliveryOrderDetailsActivity, "Failed to load order details.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun acceptOrder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            executeAcceptOrder()
        }
    }

    private fun executeAcceptOrder() {
        val acceptBtn = findViewById<Button>(R.id.acceptButton)
        acceptBtn.isEnabled = false

        lifecycleScope.launch {
            try {
                val acceptedOrder = RetrofitClient.orderApi.acceptOrder(orderId)

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(this@DeliveryOrderDetailsActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                ) {
                    DeliveryNotificationHelper.showDeliveryNotification(
                        context = this@DeliveryOrderDetailsActivity,
                        orderId = acceptedOrder.id,
                        restaurantName = acceptedOrder.restaurantName,
                        deliveryAddress = acceptedOrder.deliveryAddress
                    )
                }

                Toast.makeText(this@DeliveryOrderDetailsActivity, "Delivery accepted!", Toast.LENGTH_SHORT).show()
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@DeliveryOrderDetailsActivity, "Could not accept order.", Toast.LENGTH_LONG).show()
                acceptBtn.isEnabled = true
            }
        }
    }

    private fun finishDelivery() {
        val acceptBtn = findViewById<Button>(R.id.acceptButton)
        val declineBtn = findViewById<Button>(R.id.declineButton)

        acceptBtn.isEnabled = false
        declineBtn.isEnabled = false

        lifecycleScope.launch {
            try {
                RetrofitClient.orderApi.finishDelivery(orderId)
                DeliveryNotificationHelper.cancelDeliveryNotification(this@DeliveryOrderDetailsActivity)
                Toast.makeText(this@DeliveryOrderDetailsActivity, "Delivery completed!", Toast.LENGTH_SHORT).show()
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@DeliveryOrderDetailsActivity, "Could not complete delivery.", Toast.LENGTH_LONG).show()
                acceptBtn.isEnabled = true
                declineBtn.isEnabled = true
            }
        }
    }

    private fun cancelDelivery() {
        val acceptBtn = findViewById<Button>(R.id.acceptButton)
        val declineBtn = findViewById<Button>(R.id.declineButton)

        acceptBtn.isEnabled = false
        declineBtn.isEnabled = false

        lifecycleScope.launch {
            try {
                RetrofitClient.orderApi.cancelDelivery(orderId)
                DeliveryNotificationHelper.cancelDeliveryNotification(this@DeliveryOrderDetailsActivity)
                Toast.makeText(this@DeliveryOrderDetailsActivity, "Delivery cancelled.", Toast.LENGTH_SHORT).show()
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@DeliveryOrderDetailsActivity, "Could not cancel delivery.", Toast.LENGTH_LONG).show()
                acceptBtn.isEnabled = true
                declineBtn.isEnabled = true
            }
        }
    }
}