package com.isekco.vestia.domain.model

import java.math.BigDecimal

/**
 * Domain model representing exchange rates used for portfolio valuation.
 *
 * baseCurrency:
 *   Reference currency for valuation (Vestia uses TRY).
 *
 * timestamp:
 *   Epoch milliseconds indicating when the rates were fetched.
 *
 * rates:
 *   Map of currency -> rate relative to baseCurrency.
 *   Example:
 *       USD -> 39.25
 *       EUR -> 42.80
 *       GBP -> 50.10
 *       XAU -> 3150.00
 */
data class Rates(
    val baseCurrency: Currency,
    val timestamp: Long,
    val rates: Map<Currency, BigDecimal>
)