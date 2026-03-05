package com.isekco.vestia.data.mapper

import com.isekco.vestia.data.dto.*
import com.isekco.vestia.domain.model.*
import java.math.RoundingMode

/**
 * Domain -> DTO mappers (write path).
 *
 * JSON sözleşmesini bozmamak için:
 * - TransactionDto.assetType:
 *   - AssetType.XAU => "XAU"
 *   - AssetType.CASH => "USD/EUR/TRY/GBP" gibi instrument üzerinden yazılır
 * - TransactionDto.unitType:
 *   - UnitType.GRAM => "g"
 *   - diğerleri => enum name ("USD", "TRY" ...)
 */
internal fun Ledger.toDto(): LedgerDto {
    return LedgerDto(
        schemaVersion = schemaVersion,
        baseCurrency = baseCurrency.name,
        owners = owners.map { it.toDto() },
        accounts = accounts.map { it.toDto() },
        transactions = transactions.map { it.toDto() }
    )
}

private fun Owner.toDto(): OwnerDto = OwnerDto(
    id = id,
    name = name
)

private fun Account.toDto(): AccountDto = AccountDto(
    id = id,
    ownerId = ownerId,
    name = name,
    currency = currency.name
)

private fun Transaction.toDto(): TransactionDto {
    val jsonAssetType = when (assetType) {
        AssetType.XAU -> "XAU"
        AssetType.CASH -> assetInstrument // USD/EUR/TRY/GBP
        AssetType.FX -> "FX"
    }

    val jsonUnitType = when (unitType) {
        UnitType.GRAM -> "g"
        else -> unitType.name
    }

    // JSON tarafında quantity/unitPrice string tutuluyor.
    // Yazarken stabil olsun diye 10 scale kullanıyoruz.
    val qty = quantity.setScale(10, RoundingMode.HALF_UP)
        .toPlainString()
        .trimEnd('0')
        .trimEnd('.')

    val price = unitPrice.setScale(10, RoundingMode.HALF_UP).toPlainString()

    return TransactionDto(
        id = id,
        ownerId = ownerId,
        accountId = accountId,
        epochMs = epochMs,
        transactionType = transactionType.name,
        assetType = jsonAssetType,
        assetInstrument = assetInstrument,
        unitType = jsonUnitType,
        quantity = qty,
        unitPrice = price,
        priceCurrency = priceCurrency.name,
        tags = tags
    )
}