package com.isekco.vestia.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isekco.vestia.domain.usecase.LoadPositionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val loadPositionsUseCase: LoadPositionsUseCase
) : ViewModel() {

    private var hasLoadedInitially: Boolean = false

    private val _uiState = MutableStateFlow(
        MainUiState(
            positions = emptyList(),
            isLoading = false,
            errorMessage = null
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        loadPositions()
    }

    fun loadPositions(forceRefresh: Boolean = false) {
        if (hasLoadedInitially && !forceRefresh) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val positions = loadPositionsUseCase.execute(forceRefresh)

                hasLoadedInitially = true
                _uiState.value = MainUiState(
                    positions = positions,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (t: Throwable) {
                _uiState.value = MainUiState(
                    positions = emptyList(),
                    isLoading = false,
                    errorMessage = t.message ?: "Unknown error"
                )
            }
        }
    }
}