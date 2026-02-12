package com.isekco.vestia.domain.model

data class Ledger(
    val schemaVersion: Int,
    val baseCurrency: Currency, // raporlama para birimi (root)
    val owners: List<Owner>,
    val accounts: List<Account>,
    val transactions: List<Transaction>
)