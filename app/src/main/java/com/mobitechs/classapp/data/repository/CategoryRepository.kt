package com.mobitechs.classapp.data.repository


import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.model.response.CategoryResponse
import com.mobitechs.classapp.data.model.response.SubCategoryResponse
import com.mobitechs.classapp.data.model.response.SubjectResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository(
    private val apiService: ApiService
) {
    suspend fun getCategories(): CategoryResponse = withContext(Dispatchers.IO) {
        val response = apiService.getCategories()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get categories: ${response.message()}")
        }
    }

    suspend fun getAllSubCategories(): SubCategoryResponse = withContext(Dispatchers.IO) {
        val response = apiService.getAllSubCategories()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get categories: ${response.message()}")
        }
    }

    suspend fun getSubCategoryByCategory(categoryId: String): SubCategoryResponse = withContext(Dispatchers.IO) {
//        val response = apiService.getSubCategoryByCategory(categoryId)
        val response = apiService.getAllSubCategories()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get categories: ${response.message()}")
        }
    }

    suspend fun getAllSubject(): SubjectResponse = withContext(Dispatchers.IO) {
        val response = apiService.getAllSubject()
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to get categories: ${response.message()}")
        }
    }

    suspend fun getSubjectByCategory(categoryId: String): SubjectResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getSubjectByCategory(categoryId)
            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to get categories: ${response.message()}")
            }
        }

    suspend fun getSubjectsBySubcategory(subCategoryId: String): SubjectResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getSubjectsBySubcategory(subCategoryId)
            if (response.isSuccessful) {
                return@withContext response.body() ?: throw Exception("Empty response body")
            } else {
                throw Exception("Failed to get categories: ${response.message()}")
            }
        }


}