package com.isekco.vestia.di

import android.content.Context
import com.google.gson.Gson
import com.isekco.vestia.data.repository.LedgerRepositoryImpl
import com.isekco.vestia.data.datasource.LedgerDataSource
import com.isekco.vestia.domain.repository.LedgerRepository
import com.isekco.vestia.domain.usecase.LoadLedgerUseCase


class AppContainer(appContext: Context) {
    private val context: Context = appContext.applicationContext // Defensive
    private val gson: Gson = Gson()
    val ledgerDataSource = LedgerDataSource(context)
    val ledgerRepository: LedgerRepository = LedgerRepositoryImpl(ledgerDataSource, gson)
    val loadLedgerUseCase = LoadLedgerUseCase(ledgerRepository)
}
