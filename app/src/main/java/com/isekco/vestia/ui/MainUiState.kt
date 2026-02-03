package com.isekco.vestia.ui

import com.isekco.vestia.domain.model.Transaction

data class MainUiState(
    val isLoading: Boolean = false,
    val items: List<Transaction> = emptyList(),
    val errorMessage: String? = null
)
