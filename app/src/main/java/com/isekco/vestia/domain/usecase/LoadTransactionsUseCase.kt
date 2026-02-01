package com.isekco.vestia.domain.usecase

import com.isekco.vestia.domain.model.Transaction
import com.isekco.vestia.domain.repository.TransactionRepository

class LoadTransactionsUseCase(
    private val repository: TransactionRepository
) {

    fun execute(): List<Transaction> {
        return repository.loadAll()
    }
}
