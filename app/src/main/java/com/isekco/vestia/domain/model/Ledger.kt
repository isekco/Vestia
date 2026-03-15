package com.isekco.vestia.domain.model

import java.math.BigDecimal

data class Ledger(
    val schemaVersion: Int,
    val baseCurrency: Currency, // raporlama para birimi (root)
    val owners: List<Owner>,
    val accounts: List<Account>,
    val transactions: List<Transaction>
)
data class Owner(
    val id: String,
    val name: String
)
data class Account(
    val id: String,
    val ownerId: String,
    val name: String,
    val currency: Currency // Currency of account
)

data class Transaction(
    val id: String,
    val ownerId: String,
    val accountId: String,
    val epochMs: Long,

    val transactionType: TransactionType,

    val assetType: AssetType,
    val assetInstrument: AssetInstrument,

    val quantity: BigDecimal,
    val unitPrice: BigDecimal, // In terms of Base Currency (TRY for now) (TRY/AssetInstrument)

    val tags: String?
) {
    val direction: TransactionDirection
        get() = transactionType.direction

    val totalAmount: BigDecimal
        get() = quantity.multiply(unitPrice) // quantity * unitPrice

    val assetKey: AssetKey
        get() = AssetKey(
            assetType = assetType,
            assetInstrument = assetInstrument,
        )
}

