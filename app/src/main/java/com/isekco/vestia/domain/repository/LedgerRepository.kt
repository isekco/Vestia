package com.isekco.vestia.domain.repository

import com.isekco.vestia.domain.model.*

/**
 * Repository contract for obtaining exchange rates.
 */
interface LedgerRepository {
    suspend fun getLedger(forceRefresh: Boolean = false): Ledger
    suspend fun addTransaction(transaction: Transaction)
    fun invalidate()
}