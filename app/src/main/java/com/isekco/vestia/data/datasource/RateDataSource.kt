package com.isekco.vestia.data.datasource

import android.content.Context

import com.google.gson.Gson
import com.isekco.vestia.data.dto.RatesDto
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
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.TimeZone

class RateDataSource(
    private val context: Context,
    private val gson: Gson
) {
    companion object {

        /* Local file */
        const val FILE_NAME = "rates.json"

        /* Connection properties */
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 10_000

        private const val CURRENCY_RATE_URL =
            "https://static.altinkaynak.com/public/Currency"

        private const val GOLD_RATE_URL =
            "https://static.altinkaynak.com/public/Gold"
    }

    private val localFile: File
        get() = File(context.filesDir, FILE_NAME)

    suspend fun fetchRates(): String {

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
                    return normalizeTRValueType(obj["Alis"].asString)
                }
            }
            throw IllegalStateException("Rate code not found: $kod")
        }

        fun findTimeValue(obj: JsonObject): String {
            val dateTime = obj["GuncellenmeZamani"].asString

            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getDefault()

            val epochMs = sdf.parse(dateTime)?.time ?: 0L

            return epochMs.toString()
        }

        val ratesDto = RatesDto(
            baseCurrency = "TRY",
            timestamp = findTimeValue(currencyRatesArray[0].asJsonObject),
            cashRates = mapOf(
                "TRY" to "1", // Since it is same as base currency
                "USD" to findRateValue(currencyRatesArray, "USD"),
                "EUR" to findRateValue(currencyRatesArray, "EUR"),
                "GBP" to findRateValue(currencyRatesArray, "GBP")
            ),
            xauRates = mapOf(
                "GRAM" to findRateValue(goldRatesArray, "GA"),
                "CEYREK" to findRateValue(goldRatesArray, "C"),
                "YARIM" to findRateValue(goldRatesArray, "Y"),
                "TAM" to findRateValue(goldRatesArray, "T")
            )
        )

        return gson.toJson(ratesDto)
    }

    private suspend fun fetchRawJson(endpoint: String): String = withContext(Dispatchers.IO)
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
                    "Rate API request failed. Endpoint=$endpoint HTTP code=$responseCode"
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

    fun readRatesJson(): String? {
        if (!localFile.exists()) return null
        return localFile.readText()
    }

    fun writeRatesJson(json: String) {
        localFile.writeText(json)
    }
}