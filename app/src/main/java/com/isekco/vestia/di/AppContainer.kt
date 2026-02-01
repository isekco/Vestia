package com.isekco.vestia.di

import android.content.Context
import com.isekco.vestia.data.datasource.AssetsTransactionDataSource
import com.isekco.vestia.data.repository.TransactionRepositoryImpl
import com.isekco.vestia.domain.repository.TransactionRepository
import com.isekco.vestia.domain.usecase.LoadTransactionsUseCase

class AppContainer(
    appContext: Context
) {
    // Android'e bağımlı olan şeyleri burada kuruyoruz (assets okumak gibi).
    private val transactionDataSource = AssetsTransactionDataSource(appContext)

    // Domain'in istediği interface'i, data katmanındaki impl ile bağlıyoruz.
    private val transactionRepository: TransactionRepository =
        TransactionRepositoryImpl(transactionDataSource)

    // UseCase, repository interface'i üzerinden çalışır.
    val loadTransactionsUseCase = LoadTransactionsUseCase(transactionRepository)
}
