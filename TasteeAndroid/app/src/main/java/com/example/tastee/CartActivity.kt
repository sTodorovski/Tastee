package com.example.tastee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tastee.adapter.CartAdapter
import com.example.tastee.dto.CartManager
import androidx.activity.OnBackPressedCallback

class CartActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalPrice: TextView
    private lateinit var adapter: CartAdapter
    private lateinit var checkoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        checkoutButton = findViewById(R.id.checkoutButton)
        recyclerView = findViewById(R.id.cartRecyclerView)
        totalPrice = findViewById(R.id.totalPrice)

        adapter = CartAdapter(CartManager.getItems().toMutableList()) {
            updateTotal()
            updateEmptyState()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        updateTotal()
        updateEmptyState()

        checkoutButton.setOnClickListener {
            if (CartManager.getItems().isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startActivity(Intent(this, OrderDetailsActivity::class.java))
        }
    }

    private fun updateTotal() {
        totalPrice.text = "Total: $${"%.2f".format(CartManager.totalPrice())}"
    }

    private fun updateEmptyState() {
        val emptyText = findViewById<TextView>(R.id.emptyCartText)
        val isEmpty = CartManager.getItems().isEmpty()

        emptyText.visibility = if (isEmpty) TextView.VISIBLE else TextView.GONE
        recyclerView.visibility = if (isEmpty) RecyclerView.GONE else RecyclerView.VISIBLE
        totalPrice.visibility = if (isEmpty) TextView.GONE else TextView.VISIBLE
        checkoutButton.visibility = if (isEmpty) Button.GONE else Button.VISIBLE
    }
}