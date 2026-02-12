package com.isekco.vestia.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isekco.vestia.domain.usecase.LoadPositionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val loadPositionsUseCase: LoadPositionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState(isLoading = true))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadPositions()
    }

    fun loadPositions(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = MainUiState(isLoading = true)

            try {
                val positions = loadPositionsUseCase.execute(forceRefresh)
                _uiState.value = MainUiState(
                    isLoading = false,
                    positions = positions,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = MainUiState(
                    isLoading = false,
                    positions = emptyList(),
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }
}
