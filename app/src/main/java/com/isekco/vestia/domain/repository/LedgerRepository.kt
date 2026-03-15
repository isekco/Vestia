package com.isekco.vestia.domain.repository

import com.isekco.vestia.domain.model.Ledger

/**
 * Repository contract for obtaining exchange rates.
 */
interface LedgerRepository {
    suspend fun getLedger(forceRefresh: Boolean = false): Ledger
    fun invalidate()
}