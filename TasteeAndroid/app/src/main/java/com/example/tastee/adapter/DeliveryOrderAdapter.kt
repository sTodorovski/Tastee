package com.example.tastee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tastee.R
import com.example.tastee.dto.OrderResponse

class DeliveryOrderAdapter(
    private var orders: List<OrderResponse>,
    private val onOrderClick: (OrderResponse) -> Unit,
    private val showOrderId: Boolean = true,
    private val showDriverActions: Boolean = false,
    private val onAcceptClick: ((OrderResponse) -> Unit)? = null,
    private val onDeclineClick: ((OrderResponse) -> Unit)? = null
) : RecyclerView.Adapter<DeliveryOrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val restaurantLogo: ImageView = itemView.findViewById(R.id.restaurantLogo)
        val restaurantName: TextView = itemView.findViewById(R.id.restaurantName)
        val orderId: TextView = itemView.findViewById(R.id.orderId)
        val deliveryAddress: TextView = itemView.findViewById(R.id.deliveryAddress)
        val orderTotal: TextView = itemView.findViewById(R.id.orderTotal)
        val orderStatus: TextView = itemView.findViewById(R.id.orderStatus)
        val orderItems: TextView = itemView.findViewById(R.id.orderItems)
        val driverActionLayout: LinearLayout = itemView.findViewById(R.id.driverActionLayout)
        val btnAcceptOrder: Button = itemView.findViewById(R.id.btnAcceptOrder)
        val btnDeclineOrder: Button = itemView.findViewById(R.id.btnDeclineOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_delivery_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context

        holder.restaurantName.text = order.restaurantName
        holder.orderId.text = "Order #${order.id}"
        holder.orderId.visibility = if (showOrderId) View.VISIBLE else View.GONE
        holder.deliveryAddress.text = order.deliveryAddress
        holder.orderTotal.text = "€%.2f".format(order.total)
        holder.orderStatus.text = order.status
        holder.orderItems.text = order.items.joinToString("\n") { item ->
            "${item.dishName} x${item.quantity}"
        }

        if (showDriverActions) {
            holder.driverActionLayout.visibility = View.VISIBLE
            holder.btnAcceptOrder.setOnClickListener {
                onAcceptClick?.invoke(order)
            }
            holder.btnDeclineOrder.setOnClickListener {
                onDeclineClick?.invoke(order)
            }
        } else {
            holder.driverActionLayout.visibility = View.GONE
        }

        if (!order.restaurantImageUrl.isNullOrBlank()) {
            val imageUrl = if (order.restaurantImageUrl.startsWith("http")) {
                order.restaurantImageUrl
            } else {
                "http://10.0.2.2:8080${order.restaurantImageUrl}"
            }

            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(holder.restaurantLogo)
        } else {
            holder.restaurantLogo.setImageResource(R.drawable.ic_person)
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