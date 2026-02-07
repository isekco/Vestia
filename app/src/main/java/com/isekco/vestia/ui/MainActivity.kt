package com.isekco.vestia.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as VestiaApp
        val ledger = app.container.loadLedgerUseCase.execute()

        Log.d("VestiaTrace", "owners=${ledger.owners.size}")
        Log.d("VestiaTrace", "accounts=${ledger.accounts.size}")
        Log.d("VestiaTrace", "transactions=${ledger.transactions.size}")

        // İstersen ilk 3 transaction'ı da logla (debug için)
        ledger.transactions.take(3).forEach { tx ->
            Log.d("VestiaTrace", "tx id=${tx.id} owner=${tx.ownerId} acc=${tx.accountId} key=${tx.assetKey}")
        }
    }
}
