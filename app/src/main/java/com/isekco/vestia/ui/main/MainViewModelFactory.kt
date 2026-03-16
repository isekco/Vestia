package com.isekco.vestia.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.isekco.vestia.domain.usecase.LoadPortfolioSummaryUseCase

class MainViewModelFactory(
    private val loadPortfolioSummaryUseCase: LoadPortfolioSummaryUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(loadPortfolioSummaryUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}