package com.isekco.vestia.data.mapper

import com.isekco.vestia.data.dto.*
import com.isekco.vestia.domain.model.*
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * DTO (JSON/storage format) -> Domain (iş modeli) dönüşümleri.
 *
 * Kurallar (Checkpoint 12):
 * - Ledger.baseCurrency root’tan gelir (raporlama baz para)
 * - Account.currency hesabın para birimidir (settlement/native)
 * - Transaction.priceCurrency unitPrice’ın para birimidir
 * - quantity/unitPrice JSON’da string, domain’de BigDecimal
 * - totalAmount JSON input değildir; domain’de derived/computed
 */
private const val CALC_SCALE = 10
private val CALC_ROUNDING = RoundingMode.HALF_UP

fun LedgerDto.toDomain(): Ledger {
    val ownersDomain: List<Owner> = owners.map { it.toDomain() }
    val accountsDomain: List<Account> = accounts.map { it.toDomain() }

    // Lookup yapıları (filtreleme ve doğrulama için)
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
        baseCurrency = parseEnumNormalized<Currency>(raw = baseCurrency, fieldName = "baseCurrency", ctx = "ledger"),
        owners = ownersDomain,
        accounts = accountsDomain,
        transactions = transactionsDomain
    )
}

fun OwnerDto.toDomain(): Owner {
    require(id.isNotBlank()) { "Owner.id boş olamaz" }
    require(name.isNotBlank()) { "Owner.name boş olamaz (ownerId=$id)" }
    return Owner(
        id = id,
        name = name
    )
}

fun AccountDto.toDomain(): Account {
    require(id.isNotBlank()) { "Account.id boş olamaz" }
    require(ownerId.isNotBlank()) { "Account.ownerId boş olamaz (accountId=$id)" }
    require(name.isNotBlank()) { "Account.name boş olamaz (accountId=$id)" }

    // JSON’da alan adı artık "currency" olmalı.
    // Eğer DTO’yu henüz değiştirmediysen, buradaki property adını da ona göre güncelle.
    require(currency.isNotBlank()) { "Account.currency boş olamaz (accountId=$id)" }

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
 * TransactionDto -> Transaction (Domain)
 *
 * Kontroller:
 * 1) Alan doğrulama (blank / epochMs)
 * 2) Referans doğrulama (ownerId mevcut mu, accountId mevcut mu, owner-account tutarlı mı)
 */
fun TransactionDto.toDomain(
    ownerIds: Set<String>,
    accountsById: Map<String, Account>
): Transaction {
    require(id.isNotBlank()) { "Transaction.id boş olamaz" }
    require(ownerId.isNotBlank()) { "Transaction.ownerId boş olamaz (txId=$id)" }
    require(accountId.isNotBlank()) { "Transaction.accountId boş olamaz (txId=$id)" }
    require(epochMs > 0L) { "Transaction.epochMs pozitif olmalı (txId=$id)" }

    require(ownerIds.contains(ownerId)) {
        "Transaction.ownerId bulunamadı: ownerId=$ownerId (txId=$id)"
    }

    val account: Account = accountsById[accountId]
        ?: error("Transaction.accountId bulunamadı: accountId=$accountId (txId=$id)")

    // tx.ownerId, filtre kolaylığı için var => account.ownerId ile tutarlı mı?
    require(account.ownerId == ownerId) {
        "Transaction ownerId/accountId tutarsız: tx.ownerId=$ownerId, account.ownerId=${account.ownerId} (txId=$id)"
    }

    val txType: TransactionType =
        parseEnumNormalized(raw = transactionType, fieldName = "transactionType", ctx = "txId=$id")

    val aType: AssetType =
        parseAssetType(raw = assetType, ctx = "txId=$id")

    val uType: UnitType =
        parseUnitType(raw = unitType, ctx = "txId=$id")

    val qty: BigDecimal =
        parseBigDecimalScaled(raw = quantity, fieldName = "quantity", ctx = "txId=$id")
    val price: BigDecimal =
        parseBigDecimalScaled(raw = unitPrice, fieldName = "unitPrice", ctx = "txId=$id")

    // unitPrice hangi para biriminde?
    val pCur: Currency =
        parseEnumNormalized(raw = priceCurrency, fieldName = "priceCurrency", ctx = "txId=$id")

    // Basit doğrulamalar (Checkpoint 12 sınırında)
    require(qty > BigDecimal.ZERO) { "quantity 0'dan büyük olmalı (txId=$id)" }
    require(price >= BigDecimal.ZERO) { "unitPrice negatif olamaz (txId=$id)" }

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

/* --------------------------
   Helpers (manuel, küçük)
   -------------------------- */

private fun parseBigDecimalScaled(
    raw: String,
    fieldName: String,
    ctx: String
): BigDecimal {
    val s = raw.trim()
    require(s.isNotEmpty()) { "$fieldName boş olamaz ($ctx)" }
    return try {
        BigDecimal(s).setScale(CALC_SCALE, CALC_ROUNDING)
    } catch (e: NumberFormatException) {
        error("$fieldName BigDecimal parse edilemedi: '$raw' ($ctx)")
    }
}

/**
 * JSON'dan gelen enum stringlerini normalize ediyoruz:
 * - trim
 * - uppercase (TR locale problemlerini önlemek için Locale.ROOT ile)
 */
private inline fun <reified T : Enum<T>> parseEnumNormalized(
    raw: String,
    fieldName: String,
    ctx: String
): T {
    val s = raw.trim()
    require(s.isNotEmpty()) { "$fieldName boş olamaz ($ctx)" }
    val normalized = s.uppercase(java.util.Locale.ROOT)
    return try {
        enumValueOf<T>(normalized)
    } catch (e: IllegalArgumentException) {
        val allowed = enumValues<T>().joinToString(", ") { it.name }
        error("$fieldName enum parse edilemedi: '$raw' ($ctx). Allowed: $allowed")
    }
}

private fun parseUnitType(
    raw: String,
    ctx: String
): UnitType {
    val s = raw.trim().lowercase()
    require(s.isNotEmpty()) { "unitType boş olamaz ($ctx)" }

    return when (s) {

        // Ağırlık
        "g", "gram" -> UnitType.GRAM
        "ons", "ounce" -> UnitType.OUNCE

        // Para birimi bazlı
        "try" -> UnitType.TRY
        "usd" -> UnitType.USD
        "eur" -> UnitType.EUR
        "gbp" -> UnitType.GBP

        else -> error("unitType tanınmıyor: '$raw' ($ctx)")
    }
}

private fun parseAssetType(
    raw: String,
    ctx: String
): AssetType {
    val s = raw.trim().uppercase()
    require(s.isNotEmpty()) { "assetType boş olamaz ($ctx)" }

    return when (s) {

        "XAU"   -> AssetType.XAU

        // JSON’da USD / EUR assetType olarak gelmiş
        "TRY", "USD", "EUR", "GBP" -> AssetType.CASH

        else -> error("assetType tanınmıyor: '$raw' ($ctx)")
    }
}
