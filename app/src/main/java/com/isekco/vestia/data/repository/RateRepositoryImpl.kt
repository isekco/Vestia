package com.isekco.vestia.data.repository

import com.google.gson.Gson
import com.isekco.vestia.data.datasource.RateDataSource
import com.isekco.vestia.data.dto.RatesDto
import com.isekco.vestia.data.mapper.toDomain
import com.isekco.vestia.domain.model.Rates
import com.isekco.vestia.domain.repository.RateRepository

class RateRepositoryImpl(
    private val dataSource: RateDataSource,
    private val gson: Gson
) : RateRepository {

    companion object {
        private const val MAX_CACHE_AGE_MS = 15 * 60 * 1000L
    }

    @Volatile
    private var cached: Rates? = null

    @Volatile
    private var cachedAtMs: Long = 0L

    override suspend fun getRates(forceRefresh: Boolean): Rates {
        val now = System.currentTimeMillis()

        // 1) Önce memory cache
        if (!forceRefresh) {
            val memoryRates = cached
            if (memoryRates != null && isFresh(cachedAtMs, now)) {
                return memoryRates
            }
        }

        // 2) Sonra local json
        val localRates = readLocalRatesSafely()
        if ((!forceRefresh) && (localRates != null) && (isFresh(localRates.timestamp, now))) {
            updateMemoryCache(localRates, now)
            return localRates
        }

        // 3) Remote fetch dene
        return try {
            val remoteJson = dataSource.fetchRates()
            dataSource.writeRatesJson(remoteJson)

            val remoteRates = parseRates(remoteJson)
            updateMemoryCache(remoteRates, now)
            remoteRates
        } catch (e: Exception) {
            // 4) Remote başarısızsa stale local fallback
            if (localRates != null) {
                updateMemoryCache(localRates, now)
                localRates
            } else {
                throw e
            }
        }
    }

    override fun invalidate() {
        cached = null
        cachedAtMs = 0L
    }

    private fun readLocalRatesSafely(): Rates? {
        val json = dataSource.readRatesJson() ?: return null
        return try {
            parseRates(json)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseRates(json: String): Rates {
        val dto = gson.fromJson(json, RatesDto::class.java)
            ?: throw IllegalStateException("rates.json parse returned null")
        return dto.toDomain()
    }

    private fun isFresh(savedAtMs: Long, nowMs: Long): Boolean {
        if (savedAtMs <= 0L) return false
        return nowMs - savedAtMs <= MAX_CACHE_AGE_MS
    }

    private fun updateMemoryCache(rates: Rates, savedAtMs: Long) {
        cached = rates
        cachedAtMs = savedAtMs
    }
}