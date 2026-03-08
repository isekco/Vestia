package com.isekco.vestia.ui.main

import com.isekco.vestia.domain.model.Position
import com.isekco.vestia.domain.model.Rates
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

class AssetUiMapper {

    companion object {
        private const val MONEY_SCALE = 2
        private const val UNAVAILABLE_TEXT = "---"
    }

    fun map(
        positions: List<Position>,
        rates: Rates?
    ): List<AssetUiModel> {
        return positions
            .groupBy { position ->
                normalizedAssetLabel(position.key.assetKey)
            }
            .map { (assetLabel, assetPositions) ->
                val totalQuantity = assetPositions
                    .map { it.quantity }
                    .fold(BigDecimal.ZERO, BigDecimal::add)

                if (rates == null) {
                    AssetUiModel(
                        assetLabel = assetLabel,
                        quantityText = formatQuantityText(
                            assetLabel = assetLabel,
                            quantity = totalQuantity
                        ),
                        rateText = UNAVAILABLE_TEXT,
                        totalValueText = UNAVAILABLE_TEXT
                    )
                } else {
                    val tryRate = resolveTryRate(
                        assetLabel = assetLabel,
                        rates = rates
                    )

                    val totalValueTry = totalQuantity
                        .multiply(tryRate)
                        .setScale(MONEY_SCALE, RoundingMode.HALF_UP)

                    AssetUiModel(
                        assetLabel = assetLabel,
                        quantityText = formatQuantityText(
                            assetLabel = assetLabel,
                            quantity = totalQuantity
                        ),
                        rateText = formatRateText(
                            assetLabel = assetLabel,
                            rateTry = tryRate
                        ),
                        totalValueText = formatTryMoney(totalValueTry)
                    )
                }
            }
            .sortedBy { it.assetLabel }
    }

    fun calculatePortfolioTotalTry(
        positions: List<Position>,
        rates: Rates?
    ): BigDecimal? {
        if (rates == null) {
            return null
        }

        return positions
            .groupBy { position ->
                normalizedAssetLabel(position.key.assetKey)
            }
            .values
            .fold(BigDecimal.ZERO) { acc, assetPositions ->
                val assetLabel = normalizedAssetLabel(assetPositions.first().key.assetKey)

                val totalQuantity = assetPositions
                    .map { it.quantity }
                    .fold(BigDecimal.ZERO, BigDecimal::add)

                val tryRate = resolveTryRate(assetLabel, rates)

                acc + totalQuantity.multiply(tryRate)
            }
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
    }

    private fun resolveTryRate(
        assetLabel: String,
        rates: Rates
    ): BigDecimal {
        return when (assetLabel.uppercase(Locale.US)) {
            "TRY" -> BigDecimal.ONE
            "USD" -> rates.rates.entries.firstOrNull { it.key.name == "USD" }?.value ?: BigDecimal.ZERO
            "EUR" -> rates.rates.entries.firstOrNull { it.key.name == "EUR" }?.value ?: BigDecimal.ZERO
            "GBP" -> rates.rates.entries.firstOrNull { it.key.name == "GBP" }?.value ?: BigDecimal.ZERO
            "XAU" -> rates.rates.entries.firstOrNull { it.key.name == "XAU" }?.value ?: BigDecimal.ZERO
            else -> BigDecimal.ZERO
        }
    }

    private fun normalizedAssetLabel(rawAssetKey: String): String {
        val normalized = rawAssetKey.uppercase(Locale.US)

        return when {
            normalized.contains("USD") -> "USD"
            normalized.contains("XAU") || normalized.contains("GRAM") -> "XAU"
            normalized.contains("GBP") -> "GBP"
            normalized.contains("EUR") -> "EUR"
            normalized.contains("TRY") -> "TRY"
            else -> rawAssetKey
        }
    }

    private fun formatQuantityText(
        assetLabel: String,
        quantity: BigDecimal
    ): String {
        val quantityText = quantity.stripTrailingZeros().toPlainString()

        return when (assetLabel.uppercase(Locale.US)) {
            "XAU" -> "$quantityText g"
            "USD" -> "$quantityText $"
            "EUR" -> "$quantityText €"
            "GBP" -> "$quantityText £"
            "TRY" -> "₺$quantityText"
            else -> quantityText
        }
    }

    private fun formatRateText(
        assetLabel: String,
        rateTry: BigDecimal
    ): String {
        return when (assetLabel.uppercase(Locale.US)) {
            "XAU" -> "${formatTryMoney(rateTry)} / g"
            "TRY" -> "₺1.00 / TRY"
            else -> "${formatTryMoney(rateTry)} / $assetLabel"
        }
    }

    private fun formatTryMoney(value: BigDecimal): String {
        return "₺" + value.setScale(MONEY_SCALE, RoundingMode.HALF_UP).toPlainString()
    }
}