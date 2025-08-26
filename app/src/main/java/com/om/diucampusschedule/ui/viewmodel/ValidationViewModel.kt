package com.om.diucampusschedule.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.om.diucampusschedule.domain.model.ValidationData
import com.om.diucampusschedule.domain.usecase.validation.GetValidationDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ValidationUiState(
    val validationData: ValidationData = ValidationData(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ValidationViewModel @Inject constructor(
    private val getValidationDataUseCase: GetValidationDataUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ValidationUiState())
    val uiState: StateFlow<ValidationUiState> = _uiState.asStateFlow()
    
    private var currentDepartment: String? = null
    
    /**
     * Load validation data for a department
     */
    fun loadValidationData(department: String) {
        if (currentDepartment == department && _uiState.value.validationData.validBatches.isNotEmpty()) {
            // Already loaded for this department
            return
        }
        
        currentDepartment = department
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val result = getValidationDataUseCase(department)
                result.fold(
                    onSuccess = { validationData ->
                        _uiState.value = _uiState.value.copy(
                            validationData = validationData,
                            isLoading = false,
                            error = null
                        )
                        android.util.Log.d("ValidationViewModel", "Loaded validation data for $department: ${validationData.validBatches.size} batches")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Could not load validation data: ${error.message}"
                        )
                        android.util.Log.e("ValidationViewModel", "Failed to load validation data", error)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading validation data: ${e.message}"
                )
                android.util.Log.e("ValidationViewModel", "Exception loading validation data", e)
            }
        }
    }
    
    /**
     * Clear any error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Get the current validation data
     */
    fun getCurrentValidationData(): ValidationData {
        return _uiState.value.validationData
    }
}
