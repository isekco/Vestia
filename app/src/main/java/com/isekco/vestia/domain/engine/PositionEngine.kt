package com.isekco.vestia.domain.engine

import com.isekco.vestia.domain.model.Ledger
import com.isekco.vestia.domain.model.Position

/**
 * PositionEngine:
 * Ledger'dan derived (hesaplanmış) Position listesini üretir.
 *
 * Bu katman:
 * - JSON/DTO bilmez
 * - Repository bilmez
 * - UI bilmez
 * Sadece saf domain hesap yapar.
 */
interface PositionEngine {
    fun calculate(ledger: Ledger): List<Position>
}
