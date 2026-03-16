package com.isekco.vestia.data.mapper

import com.isekco.vestia.data.dto.*
import com.isekco.vestia.domain.model.Ledger.*
import com.isekco.vestia.domain.model.*
import java.math.BigDecimal


/**
 * DTO (JSON/storage format) -> Domain (business model) mappers.
 *
 * Design:
 * - Single entry point: [LedgerDto.toDomain].
 * - All other mapping helpers are private to prevent partial/inconsistent mappings.
 */
internal fun LedgerDto.toDomain(): Ledger {
    val ownersDomain: List<Owner> = owners.map { it.toDomain() }
    val accountsDomain: List<Account> = accounts.map { it.toDomain() }

    // Lookup structures for validation and fast joins.
    val ownerIds: Set<String> = ownersDomain.map { it.id }.toSet()
    val accountsById: Map<String, Account> = accountsDomain.associateBy { it.id }

    val transactionsDomain: List<Transaction> = transactions.map { dto ->
        dto.toDomain(
            ownerIds = ownerIds,
            accountsById = accountsById
        )
    }

    return Ledger(
        schemaVersion = schemaVersion,
        baseCurrency = Currency.valueOf(baseCurrency.trim().uppercase()),
        owners = ownersDomain,
        accounts = accountsDomain,
        transactions = transactionsDomain
    )
}

/**
 * Maps [OwnerDto] into [Owner].
 */
private fun OwnerDto.toDomain(): Owner {
    require(id.isNotBlank()) { "Owner.id must not be blank" }
    require(name.isNotBlank()) { "Owner.name must not be blank (ownerId=$id)" }
    return Owner(
        id = id,
        name = name
    )
}

/**
 * Maps [AccountDto] into [Account].
 */
private fun AccountDto.toDomain(): Account {
    require(id.isNotBlank()) { "Account.id must not be blank" }
    require(ownerId.isNotBlank()) { "Account.ownerId must not be blank (accountId=$id)" }
    require(name.isNotBlank()) { "Account.name must not be blank (accountId=$id)" }
    require(currency.isNotBlank()) { "Account.currency must not be blank (accountId=$id)" }

    val cur: Currency = Currency.valueOf(currency.trim().uppercase())

    return Account(
        id = id,
        ownerId = ownerId,
        name = name,
        currency = cur
    )
}

/**
 * Maps [TransactionDto] into [Transaction].
 *
 * Validation performed here:
 * 1) Field-level validation (blank fields, epochMs)
 * 2) Reference validation (owner exists, account exists, and account belongs to owner)
 */
private fun TransactionDto.toDomain(
    ownerIds: Set<String>,
    accountsById: Map<String, Account>
): Transaction {
    require(id.isNotBlank()) { "Transaction.id must not be blank" }
    require(ownerId.isNotBlank()) { "Transaction.ownerId must not be blank (txId=$id)" }
    require(accountId.isNotBlank()) { "Transaction.accountId must not be blank (txId=$id)" }
    require(epochMs > 0L) { "Transaction.epochMs must be positive (txId=$id)" }
    require(assetInstrument.isNotBlank()) { "Transaction.assetInstrument must not be blank (txId=$id)" }

    require(ownerIds.contains(ownerId)) {
        "Unknown Transaction.ownerId: ownerId=$ownerId (txId=$id)"
    }

    val account: Account = accountsById[accountId]
        ?: error("Unknown Transaction.accountId: accountId=$accountId (txId=$id)")

    // tx.ownerId is kept for easy filtering; ensure it matches the account owner.
    require(account.ownerId == ownerId) {
        "Owner/account mismatch: tx.ownerId=$ownerId, account.ownerId=${account.ownerId} (txId=$id)"
    }

    val txType: TransactionType = TransactionType.valueOf(transactionType.trim().uppercase())

    val aType: AssetType = AssetType.valueOf(assetType.trim().uppercase())

    val aInst: AssetInstrument =

        when (aType) {

            AssetType.CASH -> (CashInstrument.valueOf(assetInstrument.trim().uppercase()))

            AssetType.XAU -> (XauInstrument.valueOf(assetInstrument.trim().uppercase()))
        }


    val qty: BigDecimal = BigDecimal(quantity.trim())

    val price: BigDecimal = BigDecimal(unitPrice.trim())

    require(qty > BigDecimal.ZERO) { "quantity must be > 0 (txId=$id)" }
    require(price >= BigDecimal.ZERO) { "unitPrice must be >= 0 (txId=$id)" }

    return Transaction(
        id = id,
        ownerId = ownerId,
        accountId = accountId,
        epochMs = epochMs,

        transactionType = txType,

        assetType = aType,
        assetInstrument = aInst,

        quantity = qty,
        unitPrice = price,

        tags = tags?.takeIf { it.isNotBlank() }
    )
}