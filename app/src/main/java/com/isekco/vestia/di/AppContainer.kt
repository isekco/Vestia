package com.isekco.vestia.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.isekco.vestia.data.datasource.LedgerDataSource
import com.isekco.vestia.data.repository.LedgerRepositoryImpl
import com.isekco.vestia.domain.engine.PositionEngine
import com.isekco.vestia.domain.engine.WacPositionEngine
import com.isekco.vestia.domain.repository.LedgerRepository
import com.isekco.vestia.domain.usecase.AddTransactionUseCase
import com.isekco.vestia.domain.usecase.LoadPositionsUseCase

class AppContainer(appContext: Context) {

    private val context: Context = appContext.applicationContext

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    val ledgerDataSource = LedgerDataSource(context)

    val ledgerRepository: LedgerRepository = LedgerRepositoryImpl(ledgerDataSource, gson)

    private val positionEngine: PositionEngine = WacPositionEngine()

    val loadPositionsUseCase: LoadPositionsUseCase =
        LoadPositionsUseCase(
            ledgerRepository = ledgerRepository,
            positionEngine = positionEngine
        )

    val addTransactionUseCase: AddTransactionUseCase =
        AddTransactionUseCase(ledgerRepository)
}