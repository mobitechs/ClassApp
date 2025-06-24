package com.mobitechs.classapp.data.repository

import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.screens.search.PriceRange
import com.mobitechs.classapp.screens.search.SearchFilters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {
    companion object {
        private const val MAX_RECENT_SEARCHES = 5

        private val DEFAULT_SEARCHES = listOf(
            "PSI",
            "IPS",
            "STI",
            "Talathi",
            "Constable"
        )
    }


    suspend fun getRecentSearches(): List<String> = withContext(Dispatchers.IO) {
        sharedPrefsManager.getRecentSearches().takeIf { it.isNotEmpty() } ?: DEFAULT_SEARCHES
    }

    suspend fun addRecentSearch(query: String) = withContext(Dispatchers.IO) {
        val currentSearches = sharedPrefsManager.getRecentSearches().toMutableList()

        // Remove if already exists to avoid duplicates
        currentSearches.remove(query)

        // Add to top
        currentSearches.add(0, query)

        // Keep only top 5
        val updatedSearches = currentSearches.take(MAX_RECENT_SEARCHES)

        sharedPrefsManager.saveRecentSearches(updatedSearches)
    }

    suspend fun clearRecentSearches() = withContext(Dispatchers.IO) {
        sharedPrefsManager.clearRecentSearches()
    }

    private fun applyFilters(courses: List<Course>, filters: SearchFilters): List<Course> {
        return courses.filter { course ->
            // Price filter
            val priceMatch = when (filters.priceRange) {
                PriceRange.FREE -> course.course_price == "0"
                PriceRange.UNDER_500 -> {
                    val price = course.course_discounted_price?.toIntOrNull()
                        ?: course.course_price.toIntOrNull() ?: Int.MAX_VALUE
                    price < 500
                }

                PriceRange.UNDER_1000 -> {
                    val price = course.course_discounted_price?.toIntOrNull()
                        ?: course.course_price.toIntOrNull() ?: Int.MAX_VALUE
                    price < 1000
                }

                PriceRange.ABOVE_1000 -> {
                    val price = course.course_discounted_price?.toIntOrNull()
                        ?: course.course_price.toIntOrNull() ?: 0
                    price >= 1000
                }

                null -> true
            }

            // Rating filter
            val ratingMatch = filters.minRating?.let { minRating ->
                course.course_like >= minRating
            } ?: true

            // Category filter
            val categoryMatch = if (filters.selectedCategories.isNotEmpty()) {
                filters.selectedCategories.contains(course.category_id.toString())
            } else {
                true
            }

            priceMatch && ratingMatch && categoryMatch
        }
    }


    /**
     * Get popular searches
     */
    suspend fun getPopularSearches(): List<String> = withContext(Dispatchers.IO) {
        // This could be fetched from the API or hardcoded based on analytics
        listOf(
            "Deputy Collector",
            "Assistant Commissioner",
            "Chief Officer (CO), Municipal Corporation",
            "Assistant Regional Transport Officer (ARTO)",
            "Education Officer",
            "Child Development Project Officer",
            "Government Labor Officer",
            "Tax Assistant"
        )
    }

    /**
     * Get search suggestions based on partial query
     */
    suspend fun getSearchSuggestions(partialQuery: String): List<String> =
        withContext(Dispatchers.IO) {
            // In a real implementation, this would call an API endpoint for suggestions
            // For now, filtering popular searches
            val popularSearches = getPopularSearches()
            popularSearches.filter {
                it.contains(partialQuery, ignoreCase = true)
            }.take(5)
        }


    /**
     * Get course categories for filter
     */
    suspend fun getCategories(): List<com.mobitechs.classapp.data.model.response.CategoryItem> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCategories()
                if (response.isSuccessful) {
                    response.body()?.categories ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
}