package com.example.tastee.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tastee.DeliveryOrderDetailsActivity
import com.example.tastee.R
import com.example.tastee.dto.OrderResponse

class OrderAdapter(
    private var orders: List<OrderResponse>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderIdText: TextView = itemView.findViewById(R.id.orderIdText)
        val orderStatusText: TextView = itemView.findViewById(R.id.orderStatusText)
        val orderDateText: TextView = itemView.findViewById(R.id.orderDateText)
        val restaurantNameText: TextView = itemView.findViewById(R.id.restaurantNameText)
        val itemsText: TextView = itemView.findViewById(R.id.itemsText)
        val addressText: TextView = itemView.findViewById(R.id.addressText)
        val deliveryTypeText: TextView = itemView.findViewById(R.id.deliveryTypeText)
        val tipText: TextView = itemView.findViewById(R.id.tipText)
        val totalText: TextView = itemView.findViewById(R.id.totalText)
        val paymentMethodText: TextView = itemView.findViewById(R.id.paymentMethodText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.orderIdText.text = "Order #${order.id}"
        holder.orderStatusText.text = order.status
        holder.orderDateText.text = formatDate(order.createdAt)
        holder.restaurantNameText.text = order.restaurantName
        holder.paymentMethodText.text = when (order.paymentMethod) {
            "CASH" -> "CASH"
            "CARD" -> "CARD"
            else -> "Payment: ${order.paymentMethod}"
        }

        val itemsText = buildString {
            append("Items:\n")
            for (item in order.items) {
                append("• ")
                append(item.quantity)
                append(" × ")
                append(item.dishName)
                append(" — $")
                append(String.format("%.2f", item.price))
                append("\n")
            }
        }

        holder.itemsText.text = itemsText.trimEnd()
        holder.addressText.text = "Address: ${order.deliveryAddress}"
        holder.deliveryTypeText.text = if (order.expressDelivery) {
            "Express Delivery"
        } else {
            "Standard Delivery"
        }
        holder.tipText.text = "Tip: $%.2f".format(order.tip)
        holder.totalText.text = "Total: $%.2f".format(order.total)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DeliveryOrderDetailsActivity::class.java)
            intent.putExtra("ORDER_ID", order.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<OrderResponse>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String): String {
        return try {
            val parts = dateString.split("T")
            if (parts.size == 2) {
                val date = parts[0]
                val time = parts[1].substringBefore(".")
                "$date $time"
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }
}