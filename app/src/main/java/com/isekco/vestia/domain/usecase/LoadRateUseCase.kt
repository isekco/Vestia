package com.isekco.vestia.domain.usecase

import com.isekco.vestia.domain.model.Rates
import com.isekco.vestia.domain.repository.RateRepository

class LoadRatesUseCase(
    private val rateRepository: RateRepository
) {

    suspend fun execute(forceRefresh: Boolean = false): Rates {
        return rateRepository.getRates(forceRefresh = forceRefresh)
    }
}