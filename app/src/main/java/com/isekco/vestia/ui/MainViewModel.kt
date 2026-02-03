package com.isekco.vestia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isekco.vestia.domain.usecase.LoadTransactionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val loadTransactionsUseCase: LoadTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                loadTransactionsUseCase.execute()
            }.onSuccess { transactions ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = transactions,
                        errorMessage = null
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = emptyList(),
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }
}
