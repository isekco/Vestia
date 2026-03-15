package com.isekco.vestia.data.dto

/**
 * DTO representing exchange rate response from remote API.
 *
 * Example JSON:
 *
 * {
 *   "base": "TRY",
 *   "cashRates": {
 *     "USD": 39.25,
 *     "EUR": 42.80,
 *     "GBP": 50.10
 *   },
 *   "xauRates" : {
 *      "GRAM"  : 8000.00,
 *      "CEYREK" : 11000.00,
 *      "YARIM" : 16000.00,
 *   }
 * }
 */
data class RatesDto(
    val baseCurrency: String,
    val timestamp: String,
    val cashRates: Map<String, String>,
    val xauRates: Map<String, String>
)