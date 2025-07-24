package com.mobitechs.classapp.screens.batches


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.classapp.data.model.BatchItem
import com.mobitechs.classapp.data.model.StudyMaterial
import com.mobitechs.classapp.data.repository.BatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BatchesUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val batches: List<BatchItem> = emptyList(),
    val selectedBatchId: String = "",
    val selectedBatchName: String = "",
    val studyMaterials: List<StudyMaterial> = emptyList(),
    val materialType: String = "",
    val searchQuery: String = "",
    val selectedFilter: String = "",
    val isJoiningBatch: Boolean = false,
    val batchCode: String = "",
    val batchCodeError: String = ""
)

class BatchViewModel(
    private val batchRepository: BatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchesUiState(isLoading = true))
    val uiState: StateFlow<BatchesUiState> = _uiState.asStateFlow()

    init {
        loadBatches()
    }

    fun loadBatches() {
        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                val batches = batchRepository.getUserBatches().batches

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        batches = batches,
                        selectedBatchId = batches.firstOrNull()?.batche_id ?: "",
                        selectedBatchName = batches.firstOrNull()?.batche_name ?: ""
                    )
                }

                // Load study materials for the selected batch
                if (batches.isNotEmpty()) {
                    loadStudyMaterials(batches.first().batche_id)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun selectBatch(batchId: String) {
        val batch = uiState.value.batches.find { it.batche_id == batchId }
        if (batch != null) {
            _uiState.update {
                it.copy(
                    selectedBatchId = batchId,
                    selectedBatchName = batch.batche_name
                )
            }
            loadStudyMaterials(batchId)
        }
    }

    fun loadStudyMaterials(
        batchId: String,
        type: String? = null,
        query: String? = null,
        filter: String? = null
    ) {
        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                val materials = batchRepository.getBatchMaterials(
                    batchId = batchId,
                    type = type,
                    query = query,
                    filter = filter
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        studyMaterials = materials,
                        materialType = type ?: it.materialType,
                        searchQuery = query ?: it.searchQuery,
                        selectedFilter = filter ?: it.selectedFilter
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        // Load study materials with the new search query
        loadStudyMaterials(
            batchId = uiState.value.selectedBatchId,
            type = uiState.value.materialType,
            query = query,
            filter = uiState.value.selectedFilter
        )
    }

    fun applyFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }

        // Load study materials with the new filter
        loadStudyMaterials(
            batchId = uiState.value.selectedBatchId,
            type = uiState.value.materialType,
            query = uiState.value.searchQuery,
            filter = filter
        )
    }

    fun setMaterialType(type: String) {
        _uiState.update { it.copy(materialType = type) }

        // Load study materials with the new type
        loadStudyMaterials(
            batchId = uiState.value.selectedBatchId,
            type = type,
            query = uiState.value.searchQuery,
            filter = uiState.value.selectedFilter
        )
    }

    fun updateBatchCode(code: String) {
        _uiState.update {
            it.copy(
                batchCode = code,
                batchCodeError = if (code.isEmpty()) "Batch code is required" else ""
            )
        }
    }

    fun joinBatchByCode() {
        val code = uiState.value.batchCode
        if (code.isEmpty()) {
            _uiState.update { it.copy(batchCodeError = "Batch code is required") }
            return
        }

        _uiState.update { it.copy(isJoiningBatch = true, error = "") }

        viewModelScope.launch {
            try {
                val batch = batchRepository.joinBatchByCode(code)

                // Add the new batch to the list
                val updatedBatches = uiState.value.batches.toMutableList()
                updatedBatches.add(batch)

                _uiState.update {
                    it.copy(
                        isJoiningBatch = false,
                        batches = updatedBatches,
                        batchCode = "",
                        selectedBatchId = batch.batche_id,
                        selectedBatchName = batch.batche_name
                    )
                }

                // Load study materials for the new batch
                loadStudyMaterials(batch.batche_id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isJoiningBatch = false,
                        error = e.message
                            ?: "Invalid batch code or you do not have access to this batch",
                        batchCode = ""
                    )
                }
            }
        }
    }
}
