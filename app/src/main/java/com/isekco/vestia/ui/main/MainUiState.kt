package com.isekco.vestia.ui.main

import com.isekco.vestia.domain.model.Position
import com.isekco.vestia.domain.model.Rates

data class MainUiState(
    val positions: List<Position> = emptyList(),
    val rates: Rates? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)