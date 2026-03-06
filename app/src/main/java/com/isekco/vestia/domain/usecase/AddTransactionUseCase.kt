package com.isekco.vestia.domain.usecase

import com.isekco.vestia.domain.model.*
import com.isekco.vestia.domain.repository.LedgerRepository
import java.math.BigDecimal
import java.util.UUID

class AddTransactionUseCase(
    private val ledgerRepository: LedgerRepository
) {
    suspend fun execute(input: AddTransactionInput) {
        val ledger = ledgerRepository.getLedger(forceRefresh = false)

        // owner/account validasyonu
        require(ledger.owners.any { it.id == input.ownerId }) { "Unknown ownerId: ${input.ownerId}" }
        val account = ledger.accounts.firstOrNull { it.id == input.accountId }
            ?: error("Unknown accountId: ${input.accountId}")

        require(account.ownerId == input.ownerId) { "Owner/account mismatch" }

        val txType = enumValueOf<TransactionType>(input.transactionType.trim().uppercase())

        // Asset: accountId’ye göre otomatik
        val (assetType, assetInstrument, unitType) = deriveAssetFromAccountId(input.accountId)

        val qty = BigDecimal(input.quantity.trim())
        val price = BigDecimal(input.unitPrice.trim())
        require(qty > BigDecimal.ZERO) { "Quantity must be > 0" }
        require(price >= BigDecimal.ZERO) { "UnitPrice must be >= 0" }

        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            ownerId = input.ownerId,
            accountId = input.accountId,
            epochMs = input.epochMs,
            transactionType = txType,
            assetType = assetType,
            assetInstrument = assetInstrument,
            unitType = unitType,
            quantity = qty,
            unitPrice = price,
            priceCurrency = Currency.TRY, // senin kuralın: hep TRY
            tags = input.tags?.trim()?.takeIf { it.isNotEmpty() }
        )

        ledgerRepository.addTransaction(tx)
    }

    private fun deriveAssetFromAccountId(accountId: String): Triple<AssetType, String, UnitType> {
        val id = accountId.uppercase()

        return when {
            id.contains("XAU") -> Triple(AssetType.XAU, "GRAM", UnitType.GRAM)
            id.contains("USD") -> Triple(AssetType.CASH, "USD", UnitType.USD)
            id.contains("EUR") -> Triple(AssetType.CASH, "EUR", UnitType.EUR)
            id.contains("GBP") -> Triple(AssetType.CASH, "GBP", UnitType.GBP)
            id.contains("TRY") -> Triple(AssetType.CASH, "TRY", UnitType.TRY)
            else -> error("Cannot derive asset from accountId: $accountId")
        }
    }
}