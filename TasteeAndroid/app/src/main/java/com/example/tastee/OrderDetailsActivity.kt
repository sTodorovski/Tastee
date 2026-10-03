package com.example.tastee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.tastee.dto.CartItemRequest
import com.example.tastee.dto.CartManager
import com.example.tastee.dto.CreateOrderRequest
import com.example.tastee.network.RetrofitClient
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.launch

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var subtotalText: TextView
    private lateinit var deliveryText: TextView
    private lateinit var tipText: TextView
    private lateinit var totalText: TextView

    private lateinit var address: EditText
    private lateinit var deliveryGroup: RadioGroup

    private lateinit var paymentSheet: PaymentSheet

    private var deliveryPrice = 0f
    private var tip = 0f
    private var subtotal = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_order_details)

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        subtotal = CartManager.totalPrice()

        subtotalText = findViewById(R.id.subtotalText)
        deliveryText = findViewById(R.id.deliveryText)
        tipText = findViewById(R.id.tipText)
        totalText = findViewById(R.id.totalText)

        address = findViewById(R.id.addressEditText)
        deliveryGroup = findViewById(R.id.deliveryGroup)

        updateSummary()
        setupTipSelection()
        setupDeliverySelection()
        setupPaymentButtons()
    }

    private fun setupTipSelection() {
        val tipGroup = findViewById<ChipGroup>(R.id.tipGroup)
        val customTipAmount = findViewById<EditText>(R.id.customTipAmount)

        tipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                return@setOnCheckedStateChangeListener
            }

            when (checkedIds.first()) {
                R.id.noTip -> {
                    tip = 0f
                    customTipAmount.visibility = View.GONE
                }
                R.id.tip2 -> {
                    tip = 2.5f
                    customTipAmount.visibility = View.GONE
                }
                R.id.tip5 -> {
                    tip = 5f
                    customTipAmount.visibility = View.GONE
                }
                R.id.tip10 -> {
                    tip = 10f
                    customTipAmount.visibility = View.GONE
                }
                R.id.customTip -> {
                    customTipAmount.visibility = View.VISIBLE
                    tip = customTipAmount.text.toString().toFloatOrNull() ?: 0f
                }
            }
            updateSummary()
        }

        customTipAmount.doAfterTextChanged {
            if (findViewById<Chip>(R.id.customTip).isChecked) {
                tip = it.toString().toFloatOrNull() ?: 0f
                updateSummary()
            }
        }
    }

    private fun setupDeliverySelection() {
        deliveryGroup.setOnCheckedChangeListener { _, checkedId ->
            deliveryPrice = when (checkedId) {
                R.id.pickUp -> 0f
                R.id.standardDelivery -> 0f
                R.id.expressDelivery -> 4.99f
                else -> 0f
            }
            updateSummary()
        }
    }

    private fun setupPaymentButtons() {
        val paymentButton = findViewById<Button>(R.id.paymentButton)
        val cashButton = findViewById<Button>(R.id.cashButton)

        paymentButton.setOnClickListener {
            if (!validateOrder()) {
                return@setOnClickListener
            }
            createOrder(paymentMethod = "CARD")
        }

        cashButton.setOnClickListener {
            if (!validateOrder()) {
                return@setOnClickListener
            }
            createOrder(paymentMethod = "CASH")
        }
    }

    private fun validateOrder(): Boolean {
        if (CartManager.getItems().isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            return false
        }

        if (address.text.toString().isBlank()) {
            address.error = "Please enter your delivery address"
            return false
        }

        return true
    }

    private fun updateSummary() {
        subtotalText.text = "Subtotal: $%.2f".format(subtotal)
        deliveryText.text = "Delivery: $%.2f".format(deliveryPrice)
        tipText.text = "Tip: $%.2f".format(tip)
        totalText.text = "Total: $%.2f".format(subtotal + deliveryPrice + tip)
    }

    private fun createOrder(paymentMethod: String) {
        lifecycleScope.launch {
            try {
                val request = CreateOrderRequest(
                    restaurantId = CartManager.getItems().first().restaurantId,
                    items = CartManager.getItems().map {
                        CartItemRequest(
                            dishId = it.dishId,
                            quantity = it.quantity
                        )
                    },
                    deliveryAddress = address.text.toString(),
                    expressDelivery = deliveryGroup.checkedRadioButtonId == R.id.expressDelivery,
                    tip = tip,
                    paymentMethod = paymentMethod
                )

                val response = RetrofitClient.orderApi.createOrder(request)

                if (paymentMethod == "CASH") {
                    handleCashOrderSuccess()
                    return@launch
                }

                val clientSecret = response.clientSecret
                if (clientSecret.isNullOrBlank()) {
                    Toast.makeText(this@OrderDetailsActivity, "Payment setup failed", Toast.LENGTH_LONG).show()
                    return@launch
                }

                startPayment(clientSecret)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@OrderDetailsActivity, "Failed creating order: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleCashOrderSuccess() {
        Toast.makeText(this, "Order placed successfully! Pay with cash on delivery.", Toast.LENGTH_LONG).show()
        CartManager.clear()
        goToMainActivity()
    }

    private fun startPayment(clientSecret: String) {
        val configuration = PaymentSheet.Configuration(
            merchantDisplayName = "Tastee"
        )
        paymentSheet.presentWithPaymentIntent(clientSecret, configuration)
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(this, "Payment successful!", Toast.LENGTH_LONG).show()
                CartManager.clear()
                goToMainActivity()
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(this, paymentSheetResult.error.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}