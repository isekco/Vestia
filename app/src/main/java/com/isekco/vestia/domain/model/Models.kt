package com.isekco.vestia.domain.model

import java.math.BigDecimal

data class Owner(
    val id: String,
    val name: String
)

data class Account(
    val id: String,
    val ownerId: String,
    val name: String,
    val baseCurrency: String // şimdilik string kalsın (TRY)
)

data class Transaction(
    val id: String,
    val ownerId: String,
    val accountId: String,
    val epochMs: Long,

    val transactionType: TransactionType,

    val assetType: AssetType,
    val assetInstrument: String, // string kalsın
    val unitType: String,        // "TRY", "USD", "g", "adet", "pay"

    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val totalAmount: BigDecimal?, // opsiyonel; istersen zorunluya çekersin

    val tags: String?
) {
    // JSON/DB’de tutmuyoruz; derived:
    val assetKey: String
        get() = "${assetType.name}|$assetInstrument|$unitType"
}

data class Ledger(
    val owners: List<Owner>,
    val accounts: List<Account>,
    val transactions: List<Transaction>
)
