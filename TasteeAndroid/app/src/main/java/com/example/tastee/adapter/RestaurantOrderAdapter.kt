package com.example.tastee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tastee.R
import com.example.tastee.dto.OrderResponse

class RestaurantOrderAdapter(
    private var orders: List<OrderResponse>,
    private val onOrderClick: (OrderResponse) -> Unit
) : RecyclerView.Adapter<RestaurantOrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderNumberText: TextView = itemView.findViewById(R.id.orderNumberText)
        val orderStatusText: TextView = itemView.findViewById(R.id.orderStatusText)
        val orderTimeText: TextView = itemView.findViewById(R.id.orderTimeText)
        val orderItemsText: TextView = itemView.findViewById(R.id.orderItemsText)
        val orderPaymentText: TextView = itemView.findViewById(R.id.orderPaymentText)
        val orderExpressText: TextView = itemView.findViewById(R.id.orderExpressText)
        val orderTotalText: TextView = itemView.findViewById(R.id.orderTotalText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.orderNumberText.text = "#${order.id}"
        holder.orderStatusText.text = order.status
        holder.orderTimeText.text = order.createdAt
        holder.orderItemsText.text = order.items.joinToString("\n") {
            "${it.quantity}x ${it.dishName} — $${"%.2f".format(it.price)}"
        }
        holder.orderPaymentText.text = "Payment: ${order.paymentMethod}"
        holder.orderTotalText.text = "$${"%.2f".format(order.total)}"
        if (order.expressDelivery) {
            holder.orderExpressText.visibility = View.VISIBLE
            holder.orderExpressText.text = "EXPRESS DELIVERY"
        } else {
            holder.orderExpressText.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            onOrderClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<OrderResponse>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}