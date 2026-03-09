package com.isekco.vestia.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.isekco.vestia.data.datasource.LedgerDataSource
import com.isekco.vestia.data.datasource.RateDataSource
import com.isekco.vestia.data.repository.LedgerRepositoryImpl
import com.isekco.vestia.data.repository.RateRepositoryImpl
import com.isekco.vestia.domain.engine.PositionEngine
import com.isekco.vestia.domain.engine.WacPositionEngine
import com.isekco.vestia.domain.repository.LedgerRepository
import com.isekco.vestia.domain.repository.RateRepository
import com.isekco.vestia.domain.usecase.AddTransactionUseCase
import com.isekco.vestia.domain.usecase.LoadPositionsUseCase
import com.isekco.vestia.domain.usecase.LoadRatesUseCase

class AppContainer(appContext: Context) {

    private val context: Context = appContext.applicationContext

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    val ledgerDataSource = LedgerDataSource(context, gson)

    val ledgerRepository: LedgerRepository = LedgerRepositoryImpl(ledgerDataSource,gson)

    val rateDataSource = RateDataSource(context, gson)

    val rateRepository: RateRepository = RateRepositoryImpl(rateDataSource)

    private val positionEngine: PositionEngine = WacPositionEngine()

    val loadPositionsUseCase: LoadPositionsUseCase =
        LoadPositionsUseCase(
            ledgerRepository = ledgerRepository,
            positionEngine = positionEngine
        )

    val loadRatesUseCase: LoadRatesUseCase =
        LoadRatesUseCase(
            rateRepository = rateRepository
        )

    val addTransactionUseCase: AddTransactionUseCase =
        AddTransactionUseCase(
            ledgerRepository = ledgerRepository
        )
}