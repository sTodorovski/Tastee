package com.example.tastee.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tastee.fragment.CategoriesFragment
import com.example.tastee.fragment.RestaurantsFragment

class MainPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RestaurantsFragment()
            1 -> CategoriesFragment()
            else -> throw IllegalArgumentException("Invalid tab position")
        }
    }
}