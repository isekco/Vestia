package com.isekco.vestia.di

import android.content.Context
import com.google.gson.Gson
import com.isekco.vestia.data.repository.AssetsLedgerRepositoryImpl
import com.isekco.vestia.domain.repository.LedgerRepository
import com.isekco.vestia.domain.usecase.LoadLedgerUseCase


class AppContainer(appContext: Context) {

    private val context: Context = appContext.applicationContext
    private val gson: Gson = Gson()

    val ledgerRepository: LedgerRepository =
        AssetsLedgerRepositoryImpl(
            appContext = context,
            gson = gson,
            assetFileName = "transactions.json"
        )

    val loadLedgerUseCase = LoadLedgerUseCase(ledgerRepository)
}
