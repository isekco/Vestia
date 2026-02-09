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
    val currency: Currency // hesabın native/settlement para birimi
)

data class Transaction(
    val id: String,
    val ownerId: String,
    val accountId: String,
    val epochMs: Long,

    val transactionType: TransactionType,

    val assetType: AssetType,
    val assetInstrument: String,

    // JSON’da "adet", "g", "pay" vs geliyor; domain’de enum olsun.
    val unitType: UnitType,

    // JSON’da string, domain’de BigDecimal
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,

    // unitPrice hangi para biriminde girildi?
    val priceCurrency: Currency,

    val tags: String?
) {
    // Derived: input değil
    val totalAmount: BigDecimal
        get() = quantity.multiply(unitPrice)

    // Derived:
    val assetKey: String
        get() = "${assetType.name}|$assetInstrument|${unitType.name}"
}

data class Ledger(
    val schemaVersion: Int,
    val baseCurrency: Currency, // raporlama para birimi (root)
    val owners: List<Owner>,
    val accounts: List<Account>,
    val transactions: List<Transaction>
)
