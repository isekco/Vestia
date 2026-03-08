package com.isekco.vestia.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isekco.vestia.domain.model.Position
import com.isekco.vestia.domain.model.Rates
import com.isekco.vestia.domain.usecase.LoadPositionsUseCase
import com.isekco.vestia.domain.usecase.LoadRatesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val loadPositionsUseCase: LoadPositionsUseCase,
    private val loadRatesUseCase: LoadRatesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        loadDashboard()
    }

    fun loadDashboard(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val positions: List<Position> = try {
                loadPositionsUseCase.execute(forceRefresh = forceRefresh)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Positions could not be loaded."
                )
                return@launch
            }

            val rates: Rates? = try {
                loadRatesUseCase.execute(forceRefresh = forceRefresh)
            } catch (_: Exception) {
                null
            }

            _uiState.value = _uiState.value.copy(
                positions = positions,
                rates = rates,
                isLoading = false,
                errorMessage = null
            )
        }
    }
}