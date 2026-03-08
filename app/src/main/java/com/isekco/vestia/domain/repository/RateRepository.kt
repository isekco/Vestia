package com.isekco.vestia.domain.repository

import com.isekco.vestia.domain.model.Rates

/**
 * Repository contract for obtaining exchange rates.
 *
 * Responsibilities:
 * - Provide the latest available rates
 * - Manage in-memory caching
 * - Allow controlled refresh
 *
 * Implementation may:
 * - fetch from remote API
 * - read/write local cache
 */
interface RateRepository {

    /**
     * Returns exchange rates.
     *
     * forceRefresh = true
     *      Bypasses in-memory cache and forces remote fetch attempt.
     *
     * forceRefresh = false
     *      Returns cached rates if available.
     */
    suspend fun getRates(forceRefresh: Boolean = false): Rates

    /**
     * Clears in-memory cache.
     * Next getRates() call will reload data.
     */
    fun invalidate()
}