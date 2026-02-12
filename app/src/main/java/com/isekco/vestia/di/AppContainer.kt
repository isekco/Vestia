package com.isekco.vestia.di

import android.content.Context
import com.google.gson.Gson
import com.isekco.vestia.data.repository.LedgerRepositoryImpl
import com.isekco.vestia.data.datasource.LedgerDataSource
import com.isekco.vestia.domain.repository.LedgerRepository
import com.isekco.vestia.domain.usecase.LoadLedgerUseCase
import com.isekco.vestia.domain.usecase.LoadPositionsUseCase
import com.isekco.vestia.domain.engine.PositionEngine
import com.isekco.vestia.domain.engine.WacPositionEngine


class AppContainer(appContext: Context) {

    private val context: Context = appContext.applicationContext // Defensive

    private val gson: Gson = Gson()

    val ledgerDataSource = LedgerDataSource(context)

    val ledgerRepository: LedgerRepository = LedgerRepositoryImpl(ledgerDataSource, gson)

    val loadLedgerUseCase = LoadLedgerUseCase(ledgerRepository)

    private val positionEngine: PositionEngine = WacPositionEngine()

    val loadPositionsUseCase: LoadPositionsUseCase =
        LoadPositionsUseCase(
            ledgerRepository = ledgerRepository,
            positionEngine = positionEngine
        )
}
