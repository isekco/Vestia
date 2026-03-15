package com.isekco.vestia.domain.usecase

import com.isekco.vestia.domain.engine.PositionEngine
import com.isekco.vestia.domain.engine.ValuationEngine
import com.isekco.vestia.domain.model.ValuedPosition
import com.isekco.vestia.domain.repository.LedgerRepository
import com.isekco.vestia.domain.repository.RateRepository

class LoadPortfolioSummaryUseCase(
    private val ledgerRepository: LedgerRepository,
    private val rateRepository: RateRepository,
    private val positionEngine: PositionEngine,
    private val valuationEngine: ValuationEngine
) {

    suspend fun execute(forceRefresh: Boolean = false): List<ValuedPosition> {
        val ledger = ledgerRepository.getLedger(forceRefresh = forceRefresh)
        val rates = rateRepository.getRates(forceRefresh = forceRefresh)
        val positions = positionEngine.calculate(ledger)
        val portfolioSummary = valuationEngine.calculate(positions, rates)

        return portfolioSummary
    }
}