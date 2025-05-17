package com.mobitechs.classapp.data.repository


import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository(
    private val apiService: ApiService
) {
    suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
        val response = apiService.getCategories()
        if (response.isSuccessful) {
            return@withContext response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get categories: ${response.message()}")
        }
    }

    suspend fun getSubcategories(categoryId: String): List<Category> = withContext(Dispatchers.IO) {
        val response = apiService.getSubcategories(categoryId)
        if (response.isSuccessful) {
            return@withContext response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get subcategories: ${response.message()}")
        }
    }
}