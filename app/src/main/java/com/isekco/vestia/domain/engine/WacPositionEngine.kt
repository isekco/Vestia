package com.isekco.vestia.domain.engine

import com.isekco.vestia.domain.model.*
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * WAC (Weighted Average Cost) tabanlı Position hesaplayıcı.
 *
 * Stateless ve deterministiktir.
 * Cache tutmaz.
 * Repository bilmez.
 */
class WacPositionEngine(
    private val scale: Int = 10,
    private val roundingMode: RoundingMode = RoundingMode.HALF_UP
) : PositionEngine {

    override fun calculate(ledger: Ledger): List<Position> {

        if (ledger.transactions.isEmpty()) return emptyList()

        // 1) Önce grouping anahtarı üret
        val grouped = ledger.transactions.groupBy { tx ->
            PositionKey(
                ownerId = tx.ownerId,
                accountId = tx.accountId,
                assetKey = tx.assetKey
            )
        }

        // 2) Her grup için WAC hesapla
        return grouped.map { (key, transactions) ->

            var quantity = BigDecimal.ZERO
            var totalCost = BigDecimal.ZERO

            // Zaman sıralı işlem (önemli)
            val sorted = transactions.sortedBy { it.epochMs }

            for (tx in sorted) {

                when (tx.direction) {

                    TransactionDirection.IN -> {
                        val cost = tx.quantity.multiply(tx.unitPrice)
                        quantity = quantity.add(tx.quantity)
                        totalCost = totalCost.add(cost)
                    }

                    TransactionDirection.OUT -> {
                        if (quantity == BigDecimal.ZERO) {
                            throw IllegalStateException(
                                "Cannot sell from zero position: $key"
                            )
                        }

                        val wac = totalCost.divide(quantity, scale, roundingMode)
                        val sellCost = tx.quantity.multiply(wac)

                        quantity = quantity.subtract(tx.quantity)
                        totalCost = totalCost.subtract(sellCost)

                        // Negatif güvenlik guard
                        if (quantity < BigDecimal.ZERO) {
                            throw IllegalStateException(
                                "Negative quantity detected for $key"
                            )
                        }

                        if (quantity == BigDecimal.ZERO) {
                            totalCost = BigDecimal.ZERO
                        }
                    }
                }
            }

            val finalWac =
                if (quantity == BigDecimal.ZERO)
                    BigDecimal.ZERO
                else
                    totalCost.divide(quantity, scale, roundingMode)

            Position(
                key = key,
                quantity = quantity,
                weightedAverageCost = finalWac,
                totalCost = totalCost
            )
        }
            .filter { it.quantity > BigDecimal.ZERO } // boş pozisyonları çıkar
    }
}
