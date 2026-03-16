package com.isekco.vestia.ui.main

import com.isekco.vestia.domain.model.AssetInstrument
import com.isekco.vestia.domain.model.AssetKey
import com.isekco.vestia.domain.model.CashInstrument
import com.isekco.vestia.domain.model.ValuedPosition
import com.isekco.vestia.domain.model.XauInstrument
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object AssetUiMapper {

    private const val MISSING_TEXT = "---"
    private val symbols = DecimalFormatSymbols(Locale.US)
    private val numberFormat = DecimalFormat("#,##0.00", symbols)
    private val moneyFormat = DecimalFormat("₺#,##0.00", symbols)

    fun toAssetUiModels(valuedPositions: List<ValuedPosition>): List<AssetUiModel> {
        if (valuedPositions.isEmpty()) return emptyList()

        return valuedPositions
            // TODO: Asset bazlı aggregation summary ekranı için uygun;
            // ancak avg cost / total cost gibi cost-basis metrikleri owner+account+asset seviyesinde anlamlı olabilir.
            // Bu metrikler eklenecekse detail screen veya ayrı aggregation strategy gerekebilir.
            .groupBy { it.position.key.assetKey }
            .map { (assetKey, items) -> toAssetSummary(assetKey, items) }
            .sortedBy { displayOrder(it.assetKey) }
            .map { summary ->
                AssetUiModel(
                    assetName = displayName(summary.assetKey.assetInstrument),
                    quantityText = formatNumber(summary.quantity),
                    rateText = summary.rateToBase?.let(::formatNumber) ?: MISSING_TEXT,
                    totalValueText = summary.marketValue?.let(::formatMoney) ?: MISSING_TEXT
                )
            }
    }

    fun toTotalPortfolioValueText(valuedPositions: List<ValuedPosition>): String {
        if (valuedPositions.isEmpty()) return MISSING_TEXT

        if (valuedPositions.any { it.marketValue == null }) {
            return MISSING_TEXT
        }

        val total = valuedPositions
            .mapNotNull { it.marketValue }
            .fold(BigDecimal.ZERO, BigDecimal::add)

        return formatMoney(total)
    }
    private fun toAssetSummary(assetKey: AssetKey, items: List<ValuedPosition>): AssetSummary {

        val totalQuantity = items.fold(BigDecimal.ZERO) { acc, vp ->
            acc.add(vp.position.quantity)
        }

        // TODO: Asset grubundaki tüm rateToBase değerlerinin aynı olduğu varsayılıyor.
        // İleride historical valuation, source-specific valuation veya farklı timestamp'li
        // rate yapıları gelirse burada consistency check eklenmeli.
        val rateToBase = items.firstOrNull()?.rateToBase

        // TODO: marketValue null policy netleştirilmeli.
        // Şu an gruptaki herhangi bir item'in marketValue alanı null ise
        // tüm asset satırının marketValue'su null sayılıyor.
        // İleride partial sum + warning yaklaşımı istenirse bu mantık değiştirilebilir.
        val marketValue =
            if (items.any { it.marketValue == null }) {
                null
            } else {
                items.fold(BigDecimal.ZERO) { acc, vp ->
                    acc.add(vp.marketValue!!)
                }
            }

        // TODO: Asset bazlı aggregation main summary ekranı için uygun;
        // ancak avg cost / total cost gibi cost-basis metrikleri owner+account+asset
        // seviyesinde daha anlamlı olabilir. Bu metrikler eklenecekse ayrı bir
        // aggregation strategy veya detail screen modeli gerekebilir.
        return AssetSummary(
            assetKey = assetKey,
            quantity = totalQuantity,
            rateToBase = rateToBase,
            marketValue = marketValue
        )
    }
    private fun displayName(instrument: AssetInstrument): String {
        return when (instrument) {
            is CashInstrument -> instrument.name
            is XauInstrument -> instrument.name
        }
    }

    // TODO: Yeni AssetInstrument tipleri eklendiğinde displayOrder ve displayName güncellenmeli.
    // Gerekirse UI sıralaması domain'den bağımsız ayrı bir config/policy nesnesine taşınabilir.
    private fun displayOrder(assetKey: AssetKey): Int {
        return when (assetKey.assetInstrument) {
            CashInstrument.TRY -> 0
            CashInstrument.USD -> 1
            CashInstrument.EUR -> 2
            CashInstrument.GBP -> 3
            XauInstrument.GRAM -> 4
            XauInstrument.CEYREK -> 5
            XauInstrument.YARIM -> 6
            XauInstrument.TAM -> 7
        }
    }

    private fun formatNumber(value: BigDecimal): String = numberFormat.format(value)
    private fun formatMoney(value: BigDecimal): String = moneyFormat.format(value)
    private data class AssetSummary(
        val assetKey: AssetKey,
        val quantity: BigDecimal,
        val rateToBase: BigDecimal?,
        val marketValue: BigDecimal?
    )
}