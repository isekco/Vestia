package com.isekco.vestia.data.dto

/**
 * DTO representing exchange rate response from remote API.
 *
 * Example JSON:
 *
 * {
 *   "base": "TRY",
 *   "rates": {
 *     "USD": 39.25,
 *     "EUR": 42.80,
 *     "GBP": 50.10
 *   }
 * }
 */
data class RatesDto(
    val base: String,
    val rates: Map<String, String>
)