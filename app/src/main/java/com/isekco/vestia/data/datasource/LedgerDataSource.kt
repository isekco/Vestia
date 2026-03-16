package com.isekco.vestia.data.datasource

import android.content.Context
import com.isekco.vestia.data.dto.*
import com.google.gson.Gson
import java.io.File

class LedgerDataSource(
    private val context: Context,
    private val gson: Gson
) {
    private companion object {
        const val FILE_NAME = "ledger.json"
    }

    private val localFile: File
        get() = File(context.filesDir, FILE_NAME)

    private fun ensureLedgerFileExists() {
        if (!localFile.exists()) {
            val ledgerDto: LedgerDto = createEmptyLedgerDto()
            localFile.writeText(gson.toJson(ledgerDto))
        }
    }

    private fun createEmptyLedgerDto(): LedgerDto {
        return LedgerDto(
            schemaVersion = 1,
            baseCurrency = "TRY",
            owners = emptyList<OwnerDto>(),
            accounts = emptyList<AccountDto>(),
            transactions = emptyList<TransactionDto>()
        )
    }

    fun readLedgerJson(): String {
        ensureLedgerFileExists()
        return localFile.readText()
    }

    fun writeLedgerJson(json: String) {
        localFile.writeText(json)
    }
}