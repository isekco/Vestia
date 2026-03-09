package com.isekco.vestia.data.datasource

import android.content.Context
import com.google.gson.Gson
import com.isekco.vestia.data.dto.RatesDto
import com.isekco.vestia.data.mapper.jsonToRates
import com.isekco.vestia.data.mapper.ratesToJson
import com.isekco.vestia.domain.model.Rates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class RateDataSource(
    private val context: Context,
    private val gson: Gson
) {

    companion object {
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 10_000

        private const val CACHE_FILE_NAME = "rates.json"
        private const val ASSET_FILE_NAME = "rates.json"

        private const val CURRENCY_RATE_URL =
            "https://static.altinkaynak.com/public/Currency"

        private const val GOLD_RATE_URL =
            "https://static.altinkaynak.com/public/Gold"
    }

    suspend fun fetchRates(): RatesDto {

        val currencyRatesJson = fetchRawJson(endpoint = CURRENCY_RATE_URL)
        val goldRatesJson = fetchRawJson(endpoint = GOLD_RATE_URL)

        val currencyRatesArray = JsonParser.parseString(currencyRatesJson).asJsonArray
        val goldRatesArray = JsonParser.parseString(goldRatesJson).asJsonArray

        fun normalizeTRValueType(value: String): String {
            return value
                .replace(".", "")
                .replace(",", ".")
        }

        fun findRateValue(array: JsonArray, kod: String): String {
            for (element in array) {
                val obj = element.asJsonObject
                if (obj["Kod"].asString == kod) {
                    return normalizeTRValueType(obj["Satis"].asString)
                }
            }
            throw IllegalStateException("Rate code not found: $kod")
        }

        val result = RatesDto(
            base = "TRY",
            rates = mapOf(
                "USD" to findRateValue(currencyRatesArray, "USD"),
                "EUR" to findRateValue(currencyRatesArray, "EUR"),
                "GBP" to findRateValue(currencyRatesArray, "GBP"),
                "XAU" to findRateValue(goldRatesArray, "GA")
            )
        )

        return result
    }
    private suspend fun fetchRawJson(endpoint : String): String = withContext(Dispatchers.IO)
    {
        val url = URL(endpoint)

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doInput = true
        }

        try {
            connection.connect()

            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                throw IllegalStateException(
                    "Rate API request failed. HTTP code: $responseCode"
                )
            }

            val responseText = BufferedReader(
                InputStreamReader(connection.inputStream)
            ).use { reader ->
                reader.readText()
            }

            return@withContext responseText

        } finally {
            connection.disconnect()
        }
    }

    fun readCachedRates(): Rates? {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        if (!file.exists()) {
            return null
        }

        return try {
            val json = file.readText(Charsets.UTF_8)
            jsonToRates(json)
        } catch (_: Exception) {
            null
        }
    }

    fun readSeedRates(): Rates {
        val json = context.assets
            .open(ASSET_FILE_NAME)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        return jsonToRates(json)
    }

    fun writeCachedRates(rates: Rates) {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        val json = ratesToJson(rates, gson)
        file.writeText(json, Charsets.UTF_8)
    }



}