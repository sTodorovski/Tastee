package com.example.tastee.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tastee.ui.MenuFragment
import com.example.tastee.ui.ReviewsFragment

class RestaurantTabsAdapter(
    activity: AppCompatActivity,
    private val restaurantId: Long
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MenuFragment.newInstance(restaurantId)
            1 -> ReviewsFragment.newInstance(restaurantId)
            else -> throw IllegalArgumentException("Invalid tab position")
        }
    }
}