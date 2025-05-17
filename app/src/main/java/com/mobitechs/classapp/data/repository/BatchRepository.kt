package com.mobitechs.classapp.data.repository


import com.mobitechs.classapp.data.api.ApiService
import com.mobitechs.classapp.data.model.Batch
import com.mobitechs.classapp.data.model.StudyMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BatchRepository(
    private val apiService: ApiService
) {

    /**
     * Get all batches for the current user
     */
    suspend fun getUserBatches(): List<Batch> = withContext(Dispatchers.IO) {
        val response = apiService.getUserBatches()
        if (response.isSuccessful) {
            return@withContext response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get batches: ${response.message()}")
        }
    }

    /**
     * Join a batch using batch code
     */
    suspend fun joinBatchByCode(batchCode: String): Batch = withContext(Dispatchers.IO) {
        val response = apiService.joinBatchByCode(batchCode)
        if (response.isSuccessful) {
            return@withContext response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to join batch: ${response.message()}")
        }
    }

    /**
     * Get study materials for a specific batch
     */
    suspend fun getBatchMaterials(
        batchId: String,
        type: String? = null,
        query: String? = null,
        filter: String? = null
    ): List<StudyMaterial> = withContext(Dispatchers.IO) {
        val response = apiService.getBatchMaterials(batchId, type, query, filter)
        if (response.isSuccessful) {
            return@withContext response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get study materials: ${response.message()}")
        }
    }
}