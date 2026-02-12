package com.isekco.vestia.ui.main

import com.isekco.vestia.domain.model.Position

data class MainUiState(
    val isLoading: Boolean = false,
    val positions: List<Position> = emptyList(),
    val errorMessage: String? = null
)
