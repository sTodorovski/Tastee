package com.example.tastee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tastee.network.RetrofitClient
import com.example.tastee.utils.DeliveryNotificationHelper
import kotlinx.coroutines.launch

class ActiveDeliveryActivity : AppCompatActivity() {
    private var orderId: Long = -1L
    private lateinit var finishDeliveryButton: Button
    private lateinit var cancelDeliveryButton: Button
    private lateinit var checkMapButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_delivery)

        orderId = intent.getLongExtra("ORDER_ID", -1L)
        if (orderId == -1L) {
            Toast.makeText(this, "Invalid delivery.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        finishDeliveryButton = findViewById(R.id.finishDeliveryButton)
        cancelDeliveryButton = findViewById(R.id.cancelDeliveryButton)
        checkMapButton = findViewById(R.id.checkMapButton)

        loadOrder()

        finishDeliveryButton.setOnClickListener {
            confirmFinishDelivery()
        }

        cancelDeliveryButton.setOnClickListener {
            confirmCancelDelivery()
        }

        checkMapButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("DELIVERY_ORDER_ID", orderId)
            startActivity(intent)
        }
    }

    private fun loadOrder() {
        lifecycleScope.launch {
            try {
                val order = RetrofitClient.orderApi.getOrderById(orderId)

                if (order.status != "DELIVERING") {
                    Toast.makeText(this@ActiveDeliveryActivity, "This delivery is no longer active.", Toast.LENGTH_LONG).show()
                    DeliveryNotificationHelper.cancelDeliveryNotification(this@ActiveDeliveryActivity)
                    finish()
                    return@launch
                }

                findViewById<TextView>(R.id.restaurantName).text = order.restaurantName
                findViewById<TextView>(R.id.orderId).text = "Order #${order.id}"
                findViewById<TextView>(R.id.deliveryAddress).text = "Delivery address: ${order.deliveryAddress}"
                findViewById<TextView>(R.id.orderItems).text = order.items.joinToString("\n") { item ->
                    "${item.dishName} x${item.quantity} — €%.2f".format(item.price * item.quantity)
                }
                findViewById<TextView>(R.id.orderTotal).text = "Total: €%.2f".format(order.total)
                findViewById<TextView>(R.id.orderTip).text = "Tip: €%.2f".format(order.tip)
                findViewById<TextView>(R.id.orderStatus).text = "Status: ${order.status}"

            } catch (e: Exception) {
                Toast.makeText(this@ActiveDeliveryActivity, "Failed to load delivery.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun finishDelivery() {
        finishDeliveryButton.isEnabled = false
        cancelDeliveryButton.isEnabled = false

        lifecycleScope.launch {
            try {
                RetrofitClient.orderApi.finishDelivery(orderId)
                DeliveryNotificationHelper.cancelDeliveryNotification(this@ActiveDeliveryActivity)
                Toast.makeText(this@ActiveDeliveryActivity, "Delivery completed!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ActiveDeliveryActivity, "Could not finish delivery.", Toast.LENGTH_LONG).show()
                finishDeliveryButton.isEnabled = true
                cancelDeliveryButton.isEnabled = true
            }
        }
    }

    private fun cancelDelivery() {
        finishDeliveryButton.isEnabled = false
        cancelDeliveryButton.isEnabled = false

        lifecycleScope.launch {
            try {
                RetrofitClient.orderApi.cancelDelivery(orderId)
                DeliveryNotificationHelper.cancelDeliveryNotification(this@ActiveDeliveryActivity)
                Toast.makeText(this@ActiveDeliveryActivity, "Delivery cancelled.", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ActiveDeliveryActivity, "Could not cancel delivery.", Toast.LENGTH_LONG).show()
                finishDeliveryButton.isEnabled = true
                cancelDeliveryButton.isEnabled = true
            }
        }
    }

    private fun confirmFinishDelivery() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Finish delivery?")
            .setMessage("Are you sure you have delivered this order to the customer?")
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("FINISH") { _, _ ->
                finishDelivery()
            }
            .show()
    }

    private fun confirmCancelDelivery() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cancel delivery?")
            .setMessage("The order will become available for another delivery driver.")
            .setNegativeButton("NO", null)
            .setPositiveButton("YES") { _, _ ->
                cancelDelivery()
            }
            .show()
    }
}