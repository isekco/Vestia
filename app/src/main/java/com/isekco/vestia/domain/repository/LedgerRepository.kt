package com.isekco.vestia.domain.repository

import com.isekco.vestia.domain.model.Ledger
import com.isekco.vestia.domain.model.Transaction

interface LedgerRepository {
    /**
     * forceRefresh=false: varsa cache döner, yoksa yükleyip cache'ler
     * forceRefresh=true : yeniden yükler ve cache'i günceller
     */
    suspend fun getLedger(forceRefresh: Boolean = false): Ledger

    /** Yeni bir transaction ekler ve kalıcı hale getirir. */
    suspend fun addTransaction(transaction: Transaction)

    /** Cache'i boşaltır. Bir sonraki getLedger() yeniden yükler. */
    fun invalidate()
}