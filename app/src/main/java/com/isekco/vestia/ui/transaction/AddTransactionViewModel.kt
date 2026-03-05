package com.isekco.vestia.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isekco.vestia.domain.usecase.AddTransactionInput
import com.isekco.vestia.domain.usecase.AddTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun submit(input: AddTransactionInput) {
        if (_uiState.value.isSaving) return

        _uiState.update { it.copy(isSaving = true, isSuccess = false, errorMessage = null) }

        viewModelScope.launch {
            runCatching {
                addTransactionUseCase.execute(input)
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, isSuccess = true, errorMessage = null) }
            }.onFailure { t ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSuccess = false,
                        errorMessage = t.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}