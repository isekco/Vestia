package com.isekco.vestia.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.isekco.vestia.data.datasource.LedgerDataSource
import com.isekco.vestia.data.datasource.RateDataSource
import com.isekco.vestia.data.repository.LedgerRepositoryImpl
import com.isekco.vestia.data.repository.RateRepositoryImpl
import com.isekco.vestia.domain.engine.PositionEngine
import com.isekco.vestia.domain.engine.ValuationEngine
import com.isekco.vestia.domain.repository.LedgerRepository
import com.isekco.vestia.domain.repository.RateRepository
import com.isekco.vestia.domain.usecase.AddOwnerUseCase
import com.isekco.vestia.domain.usecase.EditOwnerUseCase
import com.isekco.vestia.domain.usecase.LoadOwnersUseCase
import com.isekco.vestia.domain.usecase.LoadPortfolioSummaryUseCase
import kotlin.random.Random

class AppContainer(appContext: Context) {
    private val context: Context = appContext.applicationContext
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    val ledgerDataSource = LedgerDataSource(context, gson)
    val ledgerRepository: LedgerRepository = LedgerRepositoryImpl(ledgerDataSource, gson)
    val rateDataSource = RateDataSource(context, gson)
    val rateRepository: RateRepository = RateRepositoryImpl(rateDataSource, gson)
    val positionEngine: PositionEngine = PositionEngine()
    val valuationEngine: ValuationEngine = ValuationEngine()

    val loadPortfolioSummaryUseCase: LoadPortfolioSummaryUseCase =
        LoadPortfolioSummaryUseCase(
            ledgerRepository = ledgerRepository,
            rateRepository = rateRepository,
            positionEngine = positionEngine,
            valuationEngine = valuationEngine
        )

    val loadOwnersUseCase: LoadOwnersUseCase =
        LoadOwnersUseCase (
            ledgerRepository = ledgerRepository
        )

    val addOwnerUseCase: AddOwnerUseCase =
        AddOwnerUseCase(
            ledgerRepository = ledgerRepository
        )

    val editOwnerUseCase: EditOwnerUseCase =
        EditOwnerUseCase(
            ledgerRepository = ledgerRepository
        )
}