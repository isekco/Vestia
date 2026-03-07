package com.isekco.vestia.ui.main

import com.isekco.vestia.domain.model.Position

data class MainUiState(
    val positions: List<Position> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)