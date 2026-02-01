package com.isekco.vestia.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.isekco.vestia.data.datasource.AssetsTransactionDataSource
import com.isekco.vestia.data.dto.TransactionDto
import com.isekco.vestia.data.dto.toDomain
import com.isekco.vestia.domain.model.Transaction
import com.isekco.vestia.domain.repository.TransactionRepository

class TransactionRepositoryImpl(
    private val dataSource: AssetsTransactionDataSource
) : TransactionRepository {

    private val gson = Gson()

    override fun loadAll(): List<Transaction> {
        val json = dataSource.readTransactionsJson()

        val listType = object : TypeToken<List<TransactionDto>>() {}.type
        val dtos: List<TransactionDto> = gson.fromJson(json, listType)

        return dtos.map { it.toDomain() } // map equals for loop
    }
}
