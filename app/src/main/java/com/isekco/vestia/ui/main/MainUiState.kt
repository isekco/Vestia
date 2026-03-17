package com.isekco.vestia.ui.main

data class MainUiState(
    val isLoading: Boolean = false,
    val totalPortfolioValueText: String = "---",
    val assets: List<AssetTypeUiModel> = emptyList(),
    val errorMessage: String? = null
)