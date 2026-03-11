package com.isekco.vestia.data.mapper


import com.isekco.vestia.data.dto.RatesDto
import com.isekco.vestia.domain.model.Currency
import com.isekco.vestia.domain.model.Rates
import java.math.BigDecimal

/**
 * Converts API DTO to domain model.
 */
fun RatesDto.toDomain(): Rates {

    val domainRates: Map<Currency, BigDecimal> =
        rates.map {
            (key, value) -> Currency.valueOf(key) to BigDecimal(value)
        }.toMap()

    return Rates(
        baseCurrency = Currency.valueOf(baseCurrency),
        timestamp = timestamp.toLong(),
        rates = domainRates
    )
}
