package com.isekco.vestia.data.mapper


import com.isekco.vestia.data.dto.RatesDto
import com.isekco.vestia.domain.model.CashInstrument
import com.isekco.vestia.domain.model.Currency
import com.isekco.vestia.domain.model.Rates
import com.isekco.vestia.domain.model.XauInstrument
import java.math.BigDecimal

/**
 * Converts API DTO to domain model.
 */
fun RatesDto.toDomain(): Rates {

    val cRates: Map<CashInstrument, BigDecimal> =
        cashRates.map { (key, value) ->
            CashInstrument.valueOf(key) to BigDecimal(value)
        }.toMap()

    val xRates: Map<XauInstrument, BigDecimal> =
        xauRates.map { (key, value) ->
            XauInstrument.valueOf(key) to BigDecimal(value)
        }.toMap()

    return Rates(
        baseCurrency = Currency.valueOf(baseCurrency),
        timestamp = timestamp.toLong(),
        cashRates = cRates,
        xauRates = xRates,
    )
}
