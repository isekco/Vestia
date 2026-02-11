package com.isekco.vestia.data.datasource

import android.content.Context

class LedgerDataSource(
    private val context: Context,
    private val assetFileName: String = "ledger.json"
) {
    fun readLedgerJson(): String {
        return context.assets.open(assetFileName)
            .bufferedReader()
            .use { it.readText() }
    }
}
