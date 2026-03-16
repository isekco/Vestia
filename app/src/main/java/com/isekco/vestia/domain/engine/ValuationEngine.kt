package com.isekco.vestia.domain.engine

import com.isekco.vestia.domain.model.Position
import com.isekco.vestia.domain.model.AssetKey
import com.isekco.vestia.domain.model.AssetType
import com.isekco.vestia.domain.model.Currency
import com.isekco.vestia.domain.model.ValuedPosition
import com.isekco.vestia.domain.model.Rates
import java.math.BigDecimal
import java.math.RoundingMode

class ValuationEngine {

    private val moneyScale = 2
    private val roundingMode = RoundingMode.HALF_UP
    fun calculate(positions: List<Position>, rates: Rates): List<ValuedPosition> {
        if (positions.isEmpty()) {
            return emptyList()
        }

        val valuedPositions = mutableListOf<ValuedPosition>()

        for (position in positions) {

            val rateToBase = resolveRateToBase(position, rates)

            val marketValue =
                if (rateToBase != null) {
                    position.quantity
                        .multiply(rateToBase)
                        .setScale(moneyScale, roundingMode)
                } else {
                    null
                }

            valuedPositions.add(
                ValuedPosition(
                    position = position,
                    rateToBase = rateToBase,
                    marketValue = marketValue
                )
            )
        }

        return valuedPositions
    }

    private fun resolveRateToBase(position: Position, rates: Rates): BigDecimal? {

        val assetKey = position.key.assetKey

        return when (assetKey.assetType) {
            AssetType.CASH -> rates.cashRates[assetKey.assetInstrument]
            AssetType.XAU -> rates.xauRates[assetKey.assetInstrument]
        }
    }
}