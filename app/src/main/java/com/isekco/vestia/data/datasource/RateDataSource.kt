package com.isekco.vestia.data.datasource

import android.content.Context
import com.google.gson.Gson
import com.isekco.vestia.data.dto.RatesDto
import com.isekco.vestia.data.mapper.jsonToRates
import com.isekco.vestia.data.mapper.ratesToJson
import com.isekco.vestia.domain.model.Rates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

        private const val USD_TO_TRY_URL =
            "https://api.frankfurter.dev/v1/latest?base=USD&symbols=TRY"

        private const val EUR_TO_TRY_URL =
            "https://api.frankfurter.dev/v1/latest?base=EUR&symbols=TRY"

        private const val GBP_TO_TRY_URL =
            "https://api.frankfurter.dev/v1/latest?base=GBP&symbols=TRY"
    }

    suspend fun fetchRates(): List<RatesDto> = withContext(Dispatchers.IO) {
        listOf(
            fetchSingle(USD_TO_TRY_URL),
            fetchSingle(EUR_TO_TRY_URL),
            fetchSingle(GBP_TO_TRY_URL)
        )
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

    private fun fetchSingle(endpoint: String): RatesDto {
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

            return gson.fromJson(responseText, RatesDto::class.java)
                ?: throw IllegalStateException("Rate API returned empty body.")
        } finally {
            connection.disconnect()
        }
    }
}