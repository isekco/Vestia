package com.isekco.vestia.domain.repository

import com.isekco.vestia.domain.model.Transaction

interface TransactionRepository {
    fun loadAll(): List<Transaction>
}
