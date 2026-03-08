package com.isekco.vestia.data.repository

import com.isekco.vestia.data.datasource.RateDataSource
import com.isekco.vestia.data.dto.RatesDto
import com.isekco.vestia.domain.model.Currency
import com.isekco.vestia.domain.model.Rates
import com.isekco.vestia.domain.repository.RateRepository
import java.math.BigDecimal

class RateRepositoryImpl(
    private val rateDataSource: RateDataSource
) : RateRepository {

    @Volatile
    private var cached: Rates? = null

    override suspend fun getRates(forceRefresh: Boolean): Rates {
        if (!forceRefresh) {
            cached?.let { return it }
        }

        synchronized(this) {
            if (!forceRefresh) {
                cached?.let { return it }
            }
        }

        val fallbackRates = rateDataSource.readCachedRates()
            ?: rateDataSource.readSeedRates()

        val resolvedRates = try {
            val remoteDtos = rateDataSource.fetchRates()
            val mergedRates = mergeRemoteDtosWithFallback(
                remoteDtos = remoteDtos,
                fallbackRates = fallbackRates,
                timestamp = System.currentTimeMillis()
            )

            rateDataSource.writeCachedRates(mergedRates)
            mergedRates
        } catch (_: Exception) {
            fallbackRates
        }

        synchronized(this) {
            cached = resolvedRates
        }

        return resolvedRates
    }

    override fun invalidate() {
        cached = null
    }

    private fun mergeRemoteDtosWithFallback(
        remoteDtos: List<RatesDto>,
        fallbackRates: Rates,
        timestamp: Long
    ): Rates {
        val merged = fallbackRates.rates.toMutableMap()

        remoteDtos.forEach { dto ->
            val baseCurrency = Currency.valueOf(dto.base)
            val tryValue = dto.rates["TRY"]

            if (tryValue != null) {
                merged[baseCurrency] = BigDecimal.valueOf(tryValue)
            }
        }

        merged[Currency.TRY] = BigDecimal.ONE

        return Rates(
            baseCurrency = Currency.TRY,
            timestamp = timestamp,
            rates = merged
        )
    }
}