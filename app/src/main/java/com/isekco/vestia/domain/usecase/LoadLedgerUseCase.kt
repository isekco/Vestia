package com.isekco.vestia.domain.usecase

import com.isekco.vestia.domain.model.Ledger
import com.isekco.vestia.domain.repository.LedgerRepository

class LoadLedgerUseCase(
    private val ledgerRepository: LedgerRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Ledger =
        ledgerRepository.getLedger(forceRefresh)
}
