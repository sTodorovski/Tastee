package com.example.tastee.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tastee.R

class ReviewsFragment : Fragment() {
    private var restaurantId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restaurantId = requireArguments().getLong(ARG_RESTAURANT_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_reviews, container, false)
    }

    companion object {
        private const val ARG_RESTAURANT_ID = "restaurantId"

        fun newInstance(restaurantId: Long): ReviewsFragment {
            return ReviewsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_RESTAURANT_ID, restaurantId)
                }
            }
        }
    }
}