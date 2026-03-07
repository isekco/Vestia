package com.isekco.vestia.ui.main

import com.isekco.vestia.domain.model.Position
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

object AssetUiMapper {

    private val rateMap: Map<String, BigDecimal> = mapOf(
        "USD" to BigDecimal("39.25"),
        "EUR" to BigDecimal("42.80"),
        "GBP" to BigDecimal("50.10"),
        "XAU" to BigDecimal("3150.00")
    )

    fun toAssetUiModels(
        positions: List<Position>
    ): List<AssetUiModel> {
        return positions
            .groupBy { position -> normalizedAssetLabel(position.key.assetKey) }
            .map { (assetLabel, assetPositions) ->
                val totalQuantity = assetPositions
                    .map { it.quantity }
                    .fold(BigDecimal.ZERO, BigDecimal::add)

                val tryRate = rateMap[assetLabel] ?: BigDecimal.ONE

                val totalValueTry = totalQuantity
                    .multiply(tryRate)
                    .setScale(2, RoundingMode.HALF_UP)

                AssetUiModel(
                    assetKey = assetLabel,
                    assetLabel = assetLabel,
                    quantity = totalQuantity,
                    tryRate = tryRate,
                    totalValueTry = totalValueTry
                )
            }
            .sortedByDescending { it.totalValueTry }
    }

    fun calculatePortfolioTotalTry(items: List<AssetUiModel>): BigDecimal {
        return items
            .map { it.totalValueTry }
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP)
    }

    private fun normalizedAssetLabel(rawAssetKey: String): String {
        val normalized = rawAssetKey.uppercase(Locale.US)

        return when {
            normalized.contains("USD") -> "USD"
            normalized.contains("XAU") || normalized.contains("GRAM") -> "XAU"
            normalized.contains("GBP") -> "GBP"
            normalized.contains("EUR") -> "EUR"
            else -> rawAssetKey
        }
    }
}