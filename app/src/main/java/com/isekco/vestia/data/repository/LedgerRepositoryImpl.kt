package com.isekco.vestia.data.repository

import android.content.Context
import com.google.gson.Gson
import com.isekco.vestia.data.datasource.LedgerDataSource
import com.isekco.vestia.data.dto.LedgerDto
import com.isekco.vestia.data.mapper.toDomain
import com.isekco.vestia.domain.model.Ledger
import com.isekco.vestia.domain.repository.LedgerRepository

class LedgerRepositoryImpl(
    private val dataSource: LedgerDataSource,
    private val gson: Gson
) : LedgerRepository {

    @Volatile
    private var cached: Ledger? = null

    private val lock = Any()

    override suspend fun getLedger(forceRefresh: Boolean): Ledger {
        if (!forceRefresh) {
            cached?.let { return it }
        }

        return synchronized(lock) {
            if (!forceRefresh) {
                cached?.let { return@synchronized it }
            }

            val json = dataSource.readLedgerJson()
            val dto = gson.fromJson(json, LedgerDto::class.java)
            val ledger = dto.toDomain()
            cached = ledger
            ledger
        }
    }

    override fun invalidate() {
        cached = null
    }
}
