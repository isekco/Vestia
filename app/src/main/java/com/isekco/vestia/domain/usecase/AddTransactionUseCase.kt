package com.isekco.vestia.domain.usecase

import com.isekco.vestia.domain.model.AssetType
import com.isekco.vestia.domain.model.Currency
import com.isekco.vestia.domain.model.Transaction
import com.isekco.vestia.domain.model.TransactionType
import com.isekco.vestia.domain.model.UnitType
import com.isekco.vestia.domain.repository.LedgerRepository
import java.math.BigDecimal
import java.util.UUID

class AddTransactionUseCase(
    private val ledgerRepository: LedgerRepository
) {

    suspend fun execute(input: AddTransactionInput) {
        val tx = input.toDomain()
        ledgerRepository.addTransaction(tx)
    }

    private fun AddTransactionInput.toDomain(): Transaction {
        val owner = ownerId.trim()
        val account = accountId.trim()
        require(owner.isNotEmpty()) { "ownerId must not be blank" }
        require(account.isNotEmpty()) { "accountId must not be blank" }

        val txType = parseTransactionType(transactionType)

        val assetToken = assetTypeToken.trim().uppercase()
        require(assetToken.isNotEmpty()) { "assetTypeToken must not be blank" }

        val assetType = when (assetToken) {
            "XAU" -> AssetType.XAU
            "TRY", "USD", "EUR", "GBP" -> AssetType.CASH
            else -> error("Unsupported assetTypeToken: $assetToken")
        }

        val (assetInstrument, unitType) = when (assetType) {
            AssetType.XAU -> "GRAM" to UnitType.GRAM
            AssetType.CASH -> assetToken to parseUnitCurrency(assetToken)
            AssetType.FX -> error("FX not supported yet")
        }

        val qty = parseBigDecimal(quantity, "quantity")
        val price = parseBigDecimal(unitPrice, "unitPrice")
        require(qty > BigDecimal.ZERO) { "quantity must be > 0" }
        require(price >= BigDecimal.ZERO) { "unitPrice must be >= 0" }

        val pCur = parseCurrency(priceCurrency)

        return Transaction(
            id = UUID.randomUUID().toString(),
            ownerId = owner,
            accountId = account,
            epochMs = epochMs ?: System.currentTimeMillis(),

            transactionType = txType,

            assetType = assetType,
            assetInstrument = assetInstrument,
            unitType = unitType,

            quantity = qty,
            unitPrice = price,
            priceCurrency = pCur,

            tags = tags?.trim()?.takeIf { it.isNotEmpty() }
        )
    }

    private fun parseTransactionType(raw: String): TransactionType {
        val v = raw.trim().uppercase()
        return try {
            enumValueOf<TransactionType>(v)
        } catch (_: IllegalArgumentException) {
            error("Invalid transactionType: $raw")
        }
    }

    private fun parseCurrency(raw: String): Currency {
        val v = raw.trim().uppercase()
        return try {
            enumValueOf<Currency>(v)
        } catch (_: IllegalArgumentException) {
            error("Invalid currency: $raw")
        }
    }

    private fun parseUnitCurrency(token: String): UnitType {
        return when (token) {
            "TRY" -> UnitType.TRY
            "USD" -> UnitType.USD
            "EUR" -> UnitType.EUR
            "GBP" -> UnitType.GBP
            else -> error("Unsupported unit currency: $token")
        }
    }

    private fun parseBigDecimal(raw: String, field: String): BigDecimal {
        val s = raw.trim()
        require(s.isNotEmpty()) { "$field must not be blank" }
        return try {
            BigDecimal(s)
        } catch (_: NumberFormatException) {
            error("$field is not a number: '$raw'")
        }
    }
}