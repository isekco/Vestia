package com.isekco.vestia.ui.main

import com.isekco.vestia.domain.model.Position
import java.math.BigDecimal
import java.math.RoundingMode

object AssetUiMapper {

    private val rateMap: Map<String, BigDecimal> = mapOf(
        "USD" to BigDecimal("39.25"),
        "EUR" to BigDecimal("42.80"),
        "GBP" to BigDecimal("50.10"),
        "XAU" to BigDecimal("3150.00")
    )

    fun toAssetUiModels(
        positions: List<Position>,
        selectedAssetKey: String? = null
    ): List<AssetUiModel> {
        return positions
            .groupBy { position -> position.key.assetKey }
            .map { (assetKey, assetPositions) ->
                val totalQuantity = assetPositions
                    .map { it.quantity }
                    .fold(BigDecimal.ZERO, BigDecimal::add)

                val tryRate = rateMap[assetKey] ?: BigDecimal.ONE

                val totalValueTry = totalQuantity
                    .multiply(tryRate)
                    .setScale(2, RoundingMode.HALF_UP)

                AssetUiModel(
                    assetKey = assetKey,
                    quantity = totalQuantity,
                    tryRate = tryRate,
                    totalValueTry = totalValueTry,
                    isSelected = assetKey == selectedAssetKey
                )
            }
            .sortedBy { it.assetKey }
    }

    fun calculatePortfolioTotalTry(items: List<AssetUiModel>): BigDecimal {
        return items
            .map { it.totalValueTry }
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP)
    }
}