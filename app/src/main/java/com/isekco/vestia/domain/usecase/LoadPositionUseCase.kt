package com.isekco.vestia.domain.usecase

import com.isekco.vestia.domain.engine.PositionEngine
import com.isekco.vestia.domain.model.Position
import com.isekco.vestia.domain.repository.LedgerRepository

class LoadPositionsUseCase(
    private val ledgerRepository: LedgerRepository,
    private val positionEngine: PositionEngine
) {

    suspend fun execute(forceRefresh: Boolean = false): List<Position> {
        val ledger = ledgerRepository.getLedger(forceRefresh = forceRefresh)
        return positionEngine.calculate(ledger)
    }
}