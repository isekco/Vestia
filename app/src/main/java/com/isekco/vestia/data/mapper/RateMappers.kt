package com.isekco.vestia.data.mapper

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.isekco.vestia.data.dto.RatesDto
import com.isekco.vestia.domain.model.Currency
import com.isekco.vestia.domain.model.Rates
import java.math.BigDecimal

/**
 * Converts API DTO to domain model.
 */
fun RatesDto.toDomain(): Rates {

    val currencyMap = rates.mapKeys { (key, _) ->
        Currency.valueOf(key)
    }.mapValues { (_, value) ->
        BigDecimal.valueOf(value)
    }

    return Rates(
        baseCurrency = Currency.valueOf(base),
        timestamp = timestamp,
        rates = currencyMap
    )
}

/**
 * Converts JSON string (cache or asset) into domain Rates.
 */
fun jsonToRates(json: String): Rates {

    val obj = JsonParser.parseString(json).asJsonObject

    val base = Currency.valueOf(obj.get("baseCurrency").asString)
    val timestamp = obj.get("timestamp").asLong

    val ratesObject = obj.getAsJsonObject("rates")

    val rateMap = mutableMapOf<Currency, BigDecimal>()

    for ((key, value) in ratesObject.entrySet()) {
        rateMap[Currency.valueOf(key)] = BigDecimal(value.asString)
    }

    return Rates(
        baseCurrency = base,
        timestamp = timestamp,
        rates = rateMap
    )
}

/**
 * Converts domain Rates into JSON string for local cache.
 */
fun ratesToJson(rates: Rates, gson: Gson): String {

    val root = JsonObject()

    root.addProperty("baseCurrency", rates.baseCurrency.name)
    root.addProperty("timestamp", rates.timestamp)

    val ratesObject = JsonObject()

    rates.rates.forEach { (currency, value) ->
        ratesObject.addProperty(currency.name, value.toPlainString())
    }

    root.add("rates", ratesObject)

    return gson.toJson(root)
}