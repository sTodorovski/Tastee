package com.example.tastee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tastee.R
import com.example.tastee.dto.ReviewDto

class OwnerReviewsAdapter(
    private var reviews: MutableList<ReviewDto>
) : RecyclerView.Adapter<OwnerReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStars: TextView = itemView.findViewById(R.id.tvRatingStars)
        val tvComment: TextView = itemView.findViewById(R.id.tvComment)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_owner_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.tvStars.text = "⭐".repeat(review.rating)
        holder.tvComment.text = review.comment ?: "No comment provided."
        val displayName = when {
            !review.name.isNullOrBlank() -> "${review.name} ${review.surname ?: ""}".trim()
            !review.username.isNullOrBlank() -> review.username
            else -> "Customer"
        }
        holder.tvCustomerName.text = "— $displayName"
    }

    override fun getItemCount(): Int = reviews.size

    fun updateReviews(newReviews: List<ReviewDto>) {
        reviews.clear()
        reviews.addAll(newReviews)
        notifyDataSetChanged()
    }
}