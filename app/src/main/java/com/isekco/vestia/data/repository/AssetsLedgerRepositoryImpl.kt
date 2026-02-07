package com.isekco.vestia.data.repository

import android.content.Context
import com.google.gson.Gson
import com.isekco.vestia.data.dto.LedgerDto
import com.isekco.vestia.data.mapper.toDomain
import com.isekco.vestia.domain.model.Ledger
import com.isekco.vestia.domain.repository.LedgerRepository

class AssetsLedgerRepositoryImpl(
    appContext: Context,
    private val gson: Gson,
    private val assetFileName: String = "transactions.json"
) : LedgerRepository {

    private val context: Context = appContext.applicationContext

    override fun loadLedger(): Ledger {
        val json = context.assets.open(assetFileName).bufferedReader().use { it.readText() }
        val dto = gson.fromJson(json, LedgerDto::class.java)
        return dto.toDomain()
    }
}
