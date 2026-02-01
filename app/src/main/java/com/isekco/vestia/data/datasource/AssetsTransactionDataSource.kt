package com.isekco.vestia.data.datasource

import android.content.Context

class AssetsTransactionDataSource(
    private val context: Context
) {
    fun readTransactionsJson(): String {
        return context.assets.open("transactions.json")
            .bufferedReader()
            .use { it.readText() }
    }
}
