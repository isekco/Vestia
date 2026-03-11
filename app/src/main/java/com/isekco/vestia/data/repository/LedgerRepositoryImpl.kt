package com.isekco.vestia.data.repository

import com.google.gson.Gson
import com.isekco.vestia.data.datasource.LedgerDataSource
import com.isekco.vestia.data.dto.LedgerDto
import com.isekco.vestia.data.mapper.toDomain
import com.isekco.vestia.data.mapper.toDto
import com.isekco.vestia.domain.model.*
import com.isekco.vestia.domain.repository.LedgerRepository

class LedgerRepositoryImpl(
    private val dataSource: LedgerDataSource,
    private val gson: Gson
) : LedgerRepository {

    @Volatile
    private var cached: Ledger? = null

    private val lock = Any()

    override suspend fun getLedger(forceRefresh: Boolean): Ledger {
        if (!forceRefresh) cached?.let { return it }

        return synchronized(lock) {
            if (!forceRefresh) cached?.let { return@synchronized it }

            val json = dataSource.readLedgerJson()
            val dto = gson.fromJson(json, LedgerDto::class.java)
            val ledger = dto.toDomain()
            cached = ledger
            return ledger
        }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        synchronized(lock) {
            val current = cached ?: run {
                val json = dataSource.readLedgerJson()
                val dto = gson.fromJson(json, LedgerDto::class.java)
                dto.toDomain()
            }

            val updated = current.copy(
                transactions = (current.transactions + transaction)
                    .sortedWith(compareBy<Transaction> { it.epochMs }.thenBy { it.id })
            )

            val outJson = gson.toJson(updated.toDto())
            dataSource.writeLedgerJson(outJson)

            cached = updated
        }
    }

    override fun invalidate() {
        cached = null
    }
}