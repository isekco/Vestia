package com.isekco.vestia.domain.repository

import com.isekco.vestia.domain.model.Rates

/**
 * Repository contract for obtaining exchange rates.
 */
interface RateRepository {
    suspend fun getRates(forceRefresh: Boolean = false): Rates
    fun invalidate()
}