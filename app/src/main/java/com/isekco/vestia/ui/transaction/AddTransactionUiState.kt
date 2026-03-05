package com.isekco.vestia.ui.transaction

data class AddTransactionUiState(
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)