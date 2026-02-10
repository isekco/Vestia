package com.isekco.vestia.data.dto

data class OwnerDto(
    val id: String,
    val name: String
)

data class AccountDto(
    val id: String,
    val ownerId: String,
    val name: String,
    val currency: String
)

data class TransactionDto(
    val id: String,
    val ownerId: String,
    val accountId: String,
    val epochMs: Long,

    val transactionType: String,

    val assetType: String,
    val assetInstrument: String,
    val unitType: String,

    val quantity: String,
    val unitPrice: String,

    val priceCurrency: String,

    val tags: String?
)

data class LedgerDto(
    val schemaVersion: Int,
    val baseCurrency: String,
    val owners: List<OwnerDto>,
    val accounts: List<AccountDto>,
    val transactions: List<TransactionDto>
)