package com.isekco.vestia.ui.main

data class MainUiState(
    val isLoading: Boolean = false,
    val totalPortfolioValueText: String = "---",
    val assets: List<AssetUiModel> = emptyList(),
    val errorMessage: String? = null
)