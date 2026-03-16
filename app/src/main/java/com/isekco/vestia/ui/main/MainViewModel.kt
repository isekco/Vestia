package com.isekco.vestia.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isekco.vestia.domain.usecase.LoadPortfolioSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val loadPortfolioSummaryUseCase: LoadPortfolioSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState(isLoading = true))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadPortfolioSummary()
    }

    fun loadPortfolioSummary(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val valuedPositions = loadPortfolioSummaryUseCase.execute(forceRefresh)

                val assetItems = AssetUiMapper.toAssetUiModels(valuedPositions)
                val totalPortfolioValueText =
                    AssetUiMapper.toTotalPortfolioValueText(valuedPositions)

                _uiState.value = MainUiState(
                    isLoading = false,
                    totalPortfolioValueText = totalPortfolioValueText,
                    assets = assetItems,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Portfolio yüklenirken beklenmeyen bir hata oluştu."
                )
            }
        }
    }
}