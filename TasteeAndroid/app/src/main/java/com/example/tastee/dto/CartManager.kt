package com.example.tastee.dto

object CartManager {
    private val items = mutableListOf<CartItem>()

    fun addItem(item: CartItem): Boolean {
        if (items.isNotEmpty()) {
            val currentRestaurantId = items.first().restaurantId
            if (currentRestaurantId != item.restaurantId) {
                return false
            }
        }
        val existing = items.find { it.dishId == item.dishId }
        if (existing != null) {
            existing.quantity += item.quantity
        } else {
            items.add(item)
        }
        return true
    }

    fun removeOne(dishId: Long) {
        val item = items.find { it.dishId == dishId } ?: return
        item.quantity--
        if (item.quantity <= 0) {
            items.remove(item)
        }
    }

    fun getItems(): MutableList<CartItem> = items

    fun clear() {
        items.clear()
    }

    fun totalItems(): Int {
        return items.sumOf { it.quantity }
    }

    fun totalPrice(): Float {
        return items.sumOf { it.price.toDouble() * it.quantity }.toFloat()
    }

    fun removeItem(item: CartItem) {
        items.removeIf { it.dishId == item.dishId }
    }
}