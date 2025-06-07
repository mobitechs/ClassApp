package com.mobitechs.classapp.data.repository

import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.local.SharedPrefsManager
import com.mobitechs.classapp.data.model.response.Course
import com.mobitechs.classapp.screens.search.PriceRange
import com.mobitechs.classapp.screens.search.SearchFilters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository(
    private val apiService: ApiService
) {
    companion object {
        private const val RECENT_SEARCHES_KEY = "recent_searches"
        private const val MAX_RECENT_SEARCHES = 10
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
     * Get recent searches from local storage
     */
    suspend fun getRecentSearches(): List<String> = withContext(Dispatchers.IO) {
        // In a real implementation, this would be stored in SharedPreferences or Room DB
        // For now, returning sample data
        listOf(
            "Trigonometry basics",
            "Calculus for beginners",
            "Advanced algebra",
            "Physics fundamentals",
            "Chemistry 101"
        )
    }

    /**
     * Save a search term to recent searches
     */
    suspend fun saveRecentSearch(searchTerm: String) = withContext(Dispatchers.IO) {
        // In a real implementation, this would save to SharedPreferences or Room DB
        // Would also manage the list size to keep only MAX_RECENT_SEARCHES items
    }

    /**
     * Clear all recent searches
     */
    suspend fun clearRecentSearches() = withContext(Dispatchers.IO) {
        // In a real implementation, this would clear from SharedPreferences or Room DB
    }

    /**
     * Get popular searches
     */
    suspend fun getPopularSearches(): List<String> = withContext(Dispatchers.IO) {
        // This could be fetched from the API or hardcoded based on analytics
        listOf(
            "JEE Preparation",
            "NEET Biology",
            "Mathematics",
            "Physics",
            "Chemistry",
            "Computer Science",
            "English Grammar",
            "History"
        )
    }

    /**
     * Get search suggestions based on partial query
     */
    suspend fun getSearchSuggestions(partialQuery: String): List<String> = withContext(Dispatchers.IO) {
        // In a real implementation, this would call an API endpoint for suggestions
        // For now, filtering popular searches
        val popularSearches = getPopularSearches()
        popularSearches.filter {
            it.contains(partialQuery, ignoreCase = true)
        }.take(5)
    }

    /**
     * Toggle favorite status for a course
     */
    suspend fun toggleFavorite(courseId: String): Boolean = withContext(Dispatchers.IO) {
        // In a real implementation, this would call the API
        // For now, just returning true
        true
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