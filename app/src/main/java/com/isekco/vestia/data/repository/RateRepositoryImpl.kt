package com.isekco.vestia.data.repository

import com.google.gson.Gson
import com.isekco.vestia.data.datasource.RateDataSource
import com.isekco.vestia.data.dto.LedgerDto
import com.isekco.vestia.data.dto.RatesDto
import com.isekco.vestia.data.mapper.toDomain
import com.isekco.vestia.domain.model.Currency
import com.isekco.vestia.domain.model.Ledger
import com.isekco.vestia.domain.model.Rates
import com.isekco.vestia.domain.repository.RateRepository
import java.math.BigDecimal

class RateRepositoryImpl(
    private val dataSource: RateDataSource,
    private val gson: Gson
) : RateRepository {

    @Volatile
    private var cached: Rates? = null

    override suspend fun getRates(forceRefresh: Boolean): Rates {
        if (!forceRefresh) cached?.let { return it }

        synchronized(this) {
            if (!forceRefresh) cached?.let { return it }
        }
            val json = dataSource.readRatesJson()
            val dto = gson.fromJson(json, RatesDto::class.java)
            val rates = dto.toDomain()
            cached = rates
            rates
    }

    override fun invalidate() {
        cached = null
    }

}