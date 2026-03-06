package com.isekco.vestia.domain.repository

import com.isekco.vestia.domain.model.Ledger
import com.isekco.vestia.domain.model.Transaction

interface LedgerRepository {
    suspend fun getLedger(forceRefresh: Boolean = false): Ledger
    suspend fun addTransaction(transaction: Transaction)
    fun invalidate()
}