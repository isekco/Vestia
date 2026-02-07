package com.isekco.vestia.domain.repository

import com.isekco.vestia.domain.model.Ledger

interface LedgerRepository {
    fun loadLedger(): Ledger
}