package com.example.tastee.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tastee.R
import com.example.tastee.dto.CartItem
import com.example.tastee.dto.CartManager

class CartAdapter(
    private val items: MutableList<CartItem>,
    private val onCartChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.cartImage)
        val name: TextView = view.findViewById(R.id.cartName)
        val price: TextView = view.findViewById(R.id.cartPrice)
        val quantity: TextView = view.findViewById(R.id.quantityText)
        val plus: TextView = view.findViewById(R.id.plusButton)
        val minus: TextView = view.findViewById(R.id.minusButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.quantity.text = item.quantity.toString()
        holder.price.text = "$${"%.2f".format(item.price * item.quantity)}"

        val imageUrl = if (!item.image.isNullOrEmpty()) {
            if (item.image.startsWith("http")) {
                item.image
            } else {
                "http://10.0.2.2:8080${item.image}"
            }
        } else {
            null
        }

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.image)

        holder.plus.setOnClickListener {
            item.quantity++
            notifyItemChanged(holder.bindingAdapterPosition)
            onCartChanged()
        }

        holder.minus.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            item.quantity--
            if (item.quantity <= 0) {
                CartManager.removeItem(item)
                items.removeAt(currentPosition)
                notifyItemRemoved(currentPosition)
            } else {
                notifyItemChanged(currentPosition)
            }
            onCartChanged()
        }

        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Remove item?")
                .setMessage("Are you sure you want to remove ${item.name}?")
                .setPositiveButton("Remove") { _, _ ->
                    val removedPosition = holder.bindingAdapterPosition
                    if (removedPosition != RecyclerView.NO_POSITION) {
                        CartManager.removeItem(item)
                        items.removeAt(removedPosition)
                        notifyItemRemoved(removedPosition)
                        notifyItemRangeChanged(removedPosition, items.size - removedPosition)
                        onCartChanged()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}