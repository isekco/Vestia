package com.isekco.vestia.domain.model
import java.math.BigDecimal

data class Transaction(
    val id: String,
    val ownerId: String,
    val accountId: String,
    val epochMs: Long,

    val transactionType: TransactionType,

    val assetType: AssetType,
    val assetInstrument: String,
    val unitType: UnitType,

    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val priceCurrency: Currency,

    val tags: String?
) {

    val direction: TransactionDirection
        get() = transactionType.direction

    val totalAmount: BigDecimal
        get() = quantity.multiply(unitPrice) // quantity * unitPrice

    val assetKey: String
        get() = "${assetType.name}|$assetInstrument|${unitType.name}"
}

