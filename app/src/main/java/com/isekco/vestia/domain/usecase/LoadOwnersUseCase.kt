package com.isekco.vestia.domain.usecase

import com.isekco.vestia.domain.model.Owner
import com.isekco.vestia.domain.repository.LedgerRepository

class LoadOwnersUseCase(
    private val ledgerRepository: LedgerRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): List<Owner> {
        return ledgerRepository
            .getLedger(forceRefresh = forceRefresh)
            .owners
            .sortedBy { it.name.lowercase() }
    }
}