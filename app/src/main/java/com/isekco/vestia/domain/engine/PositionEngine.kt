package com.isekco.vestia.domain.engine

import com.isekco.vestia.domain.model.Ledger
import com.isekco.vestia.domain.model.Position
import com.isekco.vestia.domain.model.PositionKey
import com.isekco.vestia.domain.model.TransactionDirection
import com.isekco.vestia.domain.model.Transaction
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * PositionEngine:
 * Ledger'dan derived (hesaplanmış) Position listesini üretir.
 **/

class PositionEngine {
    private val scale = 10
    private val roundingMode = RoundingMode.HALF_UP
    fun calculate(ledger: Ledger): List<Position> {

        if (ledger.transactions.isEmpty()) {
            return emptyList()
        }

        val grouped = ledger.transactions.groupBy { tx -> buildPositionKey(tx) }
        val positions = mutableListOf<Position>()

        for ((key, transactions) in grouped) {
            val position = calculatePosition(key, transactions)

            if (position.quantity > BigDecimal.ZERO) {
                positions.add(position)
            }
        }
        return positions
    }

    private fun buildPositionKey(tx: Transaction): PositionKey {
        return PositionKey(
            ownerId = tx.ownerId,
            accountId = tx.accountId,
            assetKey = tx.assetKey
        )
    }

    private fun calculatePosition(
        key: PositionKey,
        transactions: List<Transaction>
    ): Position {

        var quantity = BigDecimal.ZERO
        var totalCost = BigDecimal.ZERO

        val sortedTransactions = transactions.sortedBy { it.epochMs }

        for (tx in sortedTransactions) {
            when (tx.direction) {

                TransactionDirection.IN -> {
                    val cost = tx.quantity.multiply(tx.unitPrice)
                    quantity = quantity.add(tx.quantity)
                    totalCost = totalCost.add(cost)
                }

                TransactionDirection.OUT -> {
                    if (quantity == BigDecimal.ZERO) {
                        throw IllegalStateException("Cannot sell from zero position: $key")
                    }

                    val weightedAverageCost =
                        totalCost.divide(quantity, scale, roundingMode)
                    val sellCost = tx.quantity.multiply(weightedAverageCost)

                    /* Burada sellCost Wac ile hesaplanıyor. Bir de gerçek satış yapıldıktan sonra realized diye bişey olması lazım */

                    quantity = quantity.subtract(tx.quantity)
                    totalCost = totalCost.subtract(sellCost)

                    if (quantity < BigDecimal.ZERO) {
                        throw IllegalStateException("Negative quantity detected for $key")
                    }

                    if (quantity == BigDecimal.ZERO) {
                        totalCost = BigDecimal.ZERO
                    }
                }
            }
        }

        val finalWeightedAverageCost =
            if (quantity == BigDecimal.ZERO) {
                BigDecimal.ZERO
            } else {
                totalCost.divide(quantity, scale, roundingMode)
            }

        return Position(
            key = key,
            quantity = quantity,
            wac = finalWeightedAverageCost,
            totalCost = totalCost
        )
    }
}