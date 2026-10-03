package com.example.tastee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tastee.R
import com.example.tastee.dto.ReviewDto

class ReviewAdapter(
    private var reviews: List<ReviewDto>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: ImageView = itemView.findViewById(R.id.reviewProfilePicture)
        val userName: TextView = itemView.findViewById(R.id.reviewUserName)
        val rating: TextView = itemView.findViewById(R.id.reviewRating)
        val comment: TextView = itemView.findViewById(R.id.reviewComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        val fullName = listOfNotNull(review.name, review.surname).joinToString(" ")
        holder.userName.text = if (fullName.isNotBlank()) {
            fullName
        } else {
            review.username ?: "User"
        }
        val rating = review.rating.coerceIn(0, 5)
        val filledStars = "★".repeat(rating)
        val emptyStars = "☆".repeat(5 - rating)
        holder.rating.text = "$filledStars$emptyStars  $rating/5"
        holder.comment.text = review.comment ?: ""
        val imageUrl = getImageUrl(review.profilePicture)
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(holder.profilePicture)
    }

    override fun getItemCount(): Int = reviews.size

    fun updateReviews(newReviews: List<ReviewDto>) {
        reviews = newReviews
        notifyDataSetChanged()
    }

    private fun getImageUrl(image: String?): String {
        if (image.isNullOrBlank()) {
            return ""
        }
        return if (image.startsWith("http")) {
            image
        } else {
            "http://10.0.2.2:8080$image"
        }
    }
}