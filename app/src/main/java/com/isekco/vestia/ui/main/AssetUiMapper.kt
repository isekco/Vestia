package com.isekco.vestia.ui.main

import com.isekco.vestia.domain.model.AssetInstrument
import com.isekco.vestia.domain.model.AssetType
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

    fun toAssetUiModels(
        valuedPositions: List<ValuedPosition>
    ): List<AssetTypeUiModel> {

        if (valuedPositions.isEmpty()) return emptyList()

        val groupedByAssetType = valuedPositions.groupBy { valuedPosition ->
            valuedPosition.position.key.assetKey.assetType }

        val sortedByAssetType  = groupedByAssetType.toSortedMap(
            compareBy { assetTypeOrder(it) })

        val assetTypeUiModel = sortedByAssetType.map { (assetType, positionsOfType) ->
            toAssetTypeUiModel(
                assetType = assetType,
                positions = positionsOfType
            )
        }

        return assetTypeUiModel
    }

    fun toTotalPortfolioValueText(
        valuedPositions: List<ValuedPosition>
    ): String {

        if (valuedPositions.isEmpty()) return MISSING_TEXT

        if (valuedPositions.any { it.marketValue == null }) {
            return MISSING_TEXT
        }

        val total = valuedPositions
            .mapNotNull { it.marketValue }
            .fold(BigDecimal.ZERO, BigDecimal::add)

        return formatMoney(total)
    }

    private fun toAssetTypeUiModel(
        assetType: AssetType,
        positions: List<ValuedPosition>
    ): AssetTypeUiModel {

        val groupedByAssetInstrument = positions.groupBy { position ->
            position.position.key.assetKey.assetInstrument
        }

        val sortedByAssetInstrument = groupedByAssetInstrument.toSortedMap(
            compareBy { instrumentOrder(it)} )


        val instrumentItems = sortedByAssetInstrument.map { (instrument, instrumentPositions) ->
            toInstrumentUiModel(
                instrument = instrument,
                positions = instrumentPositions
            )
        }

        val totalMarketValue =
            if (positions.any { it.marketValue == null }) {
                null
            } else {
                positions
                    .mapNotNull { it.marketValue }
                    .fold(BigDecimal.ZERO, BigDecimal::add)
            }

        return AssetTypeUiModel(
            assetTypeName = displayName(assetType),
            totalValueText = totalMarketValue?.let(::formatMoney) ?: MISSING_TEXT,
            isExpanded = false,
            instruments = instrumentItems
        )
    }

    private fun toInstrumentUiModel(
        instrument: AssetInstrument,
        positions: List<ValuedPosition>
    ): AssetInstrumentUiModel {

        val totalQuantity = positions.fold(BigDecimal.ZERO) { acc, vp ->
            acc.add(vp.position.quantity)
        }

        val rateToBase = positions.firstOrNull()?.rateToBase

        val totalMarketValue =
            if (positions.any { it.marketValue == null }) {
                null
            } else {
                positions
                    .mapNotNull { it.marketValue }
                    .fold(BigDecimal.ZERO, BigDecimal::add)
            }

        return AssetInstrumentUiModel(
            instrumentName = displayName(instrument),
            quantityText = formatNumber(totalQuantity),
            rateText = rateToBase?.let(::formatNumber) ?: MISSING_TEXT,
            totalValueText = totalMarketValue?.let(::formatMoney) ?: MISSING_TEXT
        )
    }

    private fun displayName(assetType: AssetType): String {
        return when (assetType) {
            AssetType.CASH -> "CASH"
            AssetType.XAU -> "XAU"
        }
    }

    private fun displayName(instrument: AssetInstrument): String {
        return when (instrument) {
            is CashInstrument -> instrument.name
            is XauInstrument -> instrument.name
        }
    }

    private fun assetTypeOrder(assetType: AssetType): Int {
        return when (assetType) {
            AssetType.CASH -> 0
            AssetType.XAU -> 1
        }
    }

    private fun instrumentOrder(instrument: AssetInstrument): Int {
        return when (instrument) {
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
}