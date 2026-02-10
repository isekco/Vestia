package com.isekco.vestia.data.mapper

import com.isekco.vestia.data.dto.*
import com.isekco.vestia.domain.model.*
import java.math.BigDecimal
import java.util.Locale

/**
 * DTO (JSON/storage format) -> Domain (business model) mappers.
 *
 * Design:
 * - Single entry point: [LedgerDto.toDomain].
 * - All other mapping helpers are private to prevent partial/inconsistent mappings.
 *
 * Notes:
 * - Ledger.baseCurrency comes from the root and represents reporting base currency.
 * - Account.currency is the account's native/settlement currency.
 * - Transaction.priceCurrency is the currency of unitPrice.
 * - JSON fields like quantity/unitPrice may be stored as String; domain uses BigDecimal.
 * - Derived values (e.g., totalAmount, assetKey, direction) are computed in domain, not stored in JSON.
 */
internal fun LedgerDto.toDomain(): Ledger {
    val ownersDomain: List<Owner> = owners.map {it.toDomain() }
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
        baseCurrency = parseEnumNormalized(raw = baseCurrency, fieldName = "baseCurrency", ctx = "ledger"),
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

    val cur: Currency =
        parseEnumNormalized(raw = currency, fieldName = "currency", ctx = "accountId=$id")

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

    val txType: TransactionType =
        parseEnumNormalized(raw = transactionType, fieldName = "transactionType", ctx = "txId=$id")

    val aType: AssetType =
        parseAssetType(raw = assetType, ctx = "txId=$id")

    val uType: UnitType =
        parseUnitType(raw = unitType, ctx = "txId=$id")

    val qty: BigDecimal =
        parseBigDecimal(raw = quantity, fieldName = "quantity", ctx = "txId=$id")

    val price: BigDecimal =
        parseBigDecimal(raw = unitPrice, fieldName = "unitPrice", ctx = "txId=$id")

    val pCur: Currency =
        parseEnumNormalized(raw = priceCurrency, fieldName = "priceCurrency", ctx = "txId=$id")

    require(qty > BigDecimal.ZERO) { "quantity must be > 0 (txId=$id)" }
    require(price >= BigDecimal.ZERO) { "unitPrice must be >= 0 (txId=$id)" }

    return Transaction(
        id = id,
        ownerId = ownerId,
        accountId = accountId,
        epochMs = epochMs,

        transactionType = txType,

        assetType = aType,
        assetInstrument = assetInstrument,
        unitType = uType,

        quantity = qty,
        unitPrice = price,
        priceCurrency = pCur,

        tags = tags?.takeIf { it.isNotBlank() }
    )
}

/* -------------------------------------------------------------------------
 * Helpers
 * ------------------------------------------------------------------------- */

/**
 * Parses a BigDecimal from a raw String.
 *
 * We intentionally do NOT enforce any fixed scale here.
 * Formatting/rounding is a presentation/reporting concern.
 */
private fun parseBigDecimal(
    raw: String,
    fieldName: String,
    ctx: String
): BigDecimal {
    val s = raw.trim()
    require(s.isNotEmpty()) { "$fieldName must not be blank ($ctx)" }
    return try {
        BigDecimal(s)
    } catch (e: NumberFormatException) {
        error("$fieldName cannot be parsed as BigDecimal: '$raw' ($ctx)")
    }
}

/**
 * Normalizes and parses enum values coming from JSON.
 *
 * Normalization rules:
 * - trim()
 * - uppercase(Locale.ROOT) to avoid locale-specific issues (e.g., Turkish i/I)
 */
private inline fun <reified T : Enum<T>> parseEnumNormalized(
    raw: String,
    fieldName: String,
    ctx: String
): T {
    val s = raw.trim()
    require(s.isNotEmpty()) { "$fieldName must not be blank ($ctx)" }
    val normalized = s.uppercase(Locale.ROOT)
    return try {
        enumValueOf<T>(normalized)
    } catch (e: IllegalArgumentException) {
        val allowed = enumValues<T>().joinToString(", ") { it.name }
        error("$fieldName cannot be parsed as enum: '$raw' ($ctx). Allowed: $allowed")
    }
}

/**
 * Maps incoming unit strings to [UnitType].
 *
 * Keep this mapping conservative and explicit; unknown values should fail fast.
 */
private fun parseUnitType(
    raw: String,
    ctx: String
): UnitType {
    val s = raw.trim().lowercase(Locale.ROOT)
    require(s.isNotEmpty()) { "unitType must not be blank ($ctx)" }

    return when (s) {
        // Weight
        "g", "gram" -> UnitType.GRAM
        "ons", "ounce" -> UnitType.OUNCE

        // Currency-like unit labels (if your JSON provides these as unit)
        "try" -> UnitType.TRY
        "usd" -> UnitType.USD
        "eur" -> UnitType.EUR
        "gbp" -> UnitType.GBP

        else -> error("Unknown unitType: '$raw' ($ctx)")
    }
}

/**
 * Maps incoming asset type strings to [AssetType].
 *
 * Notes:
 * - XAU is explicitly treated as gold.
 * - If JSON sends currencies as assetType (TRY/USD/EUR/GBP), we map them to CASH.
 */
private fun parseAssetType(
    raw: String,
    ctx: String
): AssetType {
    val s = raw.trim().uppercase(Locale.ROOT)
    require(s.isNotEmpty()) { "assetType must not be blank ($ctx)" }

    return when (s) {
        "XAU" -> AssetType.XAU
        "TRY", "USD", "EUR", "GBP" -> AssetType.CASH
        else -> error("Unknown assetType: '$raw' ($ctx)")
    }
}
