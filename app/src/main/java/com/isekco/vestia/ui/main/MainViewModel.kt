package com.isekco.vestia.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadPreviewData()
    }

    private fun loadPreviewData() {
        val previewAssets = listOf(
            AssetUiModel(
                assetName = "USD",
                quantityText = "1250.00",
                rateText = "38.12",
                totalValueText = "₺47,650.00"
            ),
            AssetUiModel(
                assetName = "EUR",
                quantityText = "820.00",
                rateText = "41.27",
                totalValueText = "₺33,841.40"
            ),
            AssetUiModel(
                assetName = "GBP",
                quantityText = "150.00",
                rateText = "48.30",
                totalValueText = "₺7,245.00"
            ),
            AssetUiModel(
                assetName = "TRY",
                quantityText = "12,500.00",
                rateText = "1.00",
                totalValueText = "₺12,500.00"
            ),
            AssetUiModel(
                assetName = "GRAM",
                quantityText = "18.50",
                rateText = "---",
                totalValueText = "---"
            ),
            AssetUiModel(
                assetName = "CEYREK",
                quantityText = "6.00",
                rateText = "---",
                totalValueText = "---"
            ),
            AssetUiModel(
                assetName = "TAM",
                quantityText = "1.00",
                rateText = "---",
                totalValueText = "---"
            )
        )

        _uiState.value = MainUiState(
            isLoading = false,
            totalPortfolioValueText = "---",
            assets = previewAssets,
            errorMessage = null
        )
    }
}