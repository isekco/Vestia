package com.isekco.vestia.domain.usecase

import com.isekco.vestia.domain.model.Ledger
import com.isekco.vestia.domain.repository.LedgerRepository

class LoadLedgerUseCase(
    private val ledgerRepository: LedgerRepository
) {
    fun execute(): Ledger {
        return ledgerRepository.loadLedger()
    }
}
