package com.isekco.vestia.data.dto

data class LedgerDto(
    val schemaVersion: Int,
    val baseCurrency: String,
    val owners: List<OwnerDto>,
    val accounts: List<AccountDto>,
    val transactions: List<TransactionDto>
)

data class OwnerDto(
    val id: String,
    val name: String
)

data class AccountDto(
    val id: String,
    val ownerId: String,
    val name: String,
    val baseCurrency: String
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

    // Precision için JSON’da string tutuyoruz:
    val quantity: String,
    val unitPrice: String,
    val totalAmount: String?,

    val tags: String?
)
