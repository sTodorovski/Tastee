package com.example.tastee.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tastee.R
import com.example.tastee.adapter.DishCategoryAdapter
import com.example.tastee.adapter.DishCategorySection
import com.example.tastee.network.RetrofitClient
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {
    private var restaurantId: Long = -1L
    private lateinit var dishRecyclerView: RecyclerView
    private lateinit var adapter: DishCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restaurantId = requireArguments().getLong(ARG_RESTAURANT_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dishRecyclerView = view.findViewById(R.id.dishRecyclerView)
        adapter = DishCategoryAdapter(emptyList()) { dish ->
            DishBottomSheet(requireContext(), dish) {}.show()
        }
        dishRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        dishRecyclerView.adapter = adapter
        loadDishes()
    }

    private fun loadDishes() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val dishes = RetrofitClient.dishApi.getRestaurantDishes(restaurantId)
                val groupedCategories = dishes
                    .filter { it.availability }
                    .groupBy { it.category?.takeIf { category -> category.isNotBlank() } ?: "Other" }
                    .map { (categoryName, categoryDishes) ->
                        DishCategorySection(
                            categoryName = categoryName,
                            dishes = categoryDishes
                        )
                    }
                adapter.updateCategories(groupedCategories)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val ARG_RESTAURANT_ID = "restaurantId"

        fun newInstance(restaurantId: Long): MenuFragment {
            return MenuFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_RESTAURANT_ID, restaurantId)
                }
            }
        }
    }
}