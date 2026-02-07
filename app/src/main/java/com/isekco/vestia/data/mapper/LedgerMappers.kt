package com.isekco.vestia.data.mapper

import com.isekco.vestia.data.dto.*
import com.isekco.vestia.domain.model.*
import java.math.BigDecimal

/**
 * DTO (JSON/storage format) -> Domain (iş modeli) dönüşümleri.
 *
 * "Magic" yok:
 * - Enum parse: enumValueOf ile manuel
 * - BigDecimal parse: BigDecimal(...) ile manuel
 * - Referans doğrulama: ownerIds ve accountsById üzerinden manuel
 */
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
    require(baseCurrency.isNotBlank()) { "Account.baseCurrency boş olamaz (accountId=$id)" }

    return Account(
        id = id,
        ownerId = ownerId,
        name = name,
        baseCurrency = baseCurrency
    )
}

/**
 * TransactionDto -> Transaction (Domain)
 *
 * Bu fonksiyonda iki tip kontrol var:
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

    // Owner var mı?
    require(ownerIds.contains(ownerId)) {
        "Transaction.ownerId bulunamadı: ownerId=$ownerId (txId=$id)"
    }

    // Account var mı?
    val account: Account = accountsById[accountId]
        ?: error("Transaction.accountId bulunamadı: accountId=$accountId (txId=$id)")

    // Senin kararın: tx.ownerId filtre kolaylığı için var.
    // Bu yüzden account.ownerId ile tutarlı mı diye kontrol ediyoruz.
    require(account.ownerId == ownerId) {
        "Transaction ownerId/accountId tutarsız: tx.ownerId=$ownerId, account.ownerId=${account.ownerId} (txId=$id)"
    }

    val txType: TransactionType =
        parseEnum(raw = transactionType, fieldName = "transactionType", ctx = "txId=$id")
    val aType: AssetType =
        parseEnum(raw = assetType, fieldName = "assetType", ctx = "txId=$id")

    require(assetInstrument.isNotBlank()) { "assetInstrument boş olamaz (txId=$id)" }
    require(unitType.isNotBlank()) { "unitType boş olamaz (txId=$id)" }

    val qty: BigDecimal =
        parseBigDecimal(raw = quantity, fieldName = "quantity", ctx = "txId=$id")
    val price: BigDecimal =
        parseBigDecimal(raw = unitPrice, fieldName = "unitPrice", ctx = "txId=$id")
    val total: BigDecimal? =
        totalAmount?.let { parseBigDecimal(raw = it, fieldName = "totalAmount", ctx = "txId=$id") }

    // Basit doğrulamalar (Checkpoint 11 sınırında)
    require(qty > BigDecimal.ZERO) { "quantity 0'dan büyük olmalı (txId=$id)" }
    require(price >= BigDecimal.ZERO) { "unitPrice negatif olamaz (txId=$id)" }
    if (total != null) require(total >= BigDecimal.ZERO) { "totalAmount negatif olamaz (txId=$id)" }

    // totalAmount varsa, qty*price ile tutarlılık kontrolü (toleranslı)
    if (total != null) {
        val computed = qty.multiply(price)
        val diff = computed.subtract(total).abs()
        if (diff > BigDecimal("0.00000001")) {
            // Crash yerine log (Checkpoint 11 rahatlatır)
            android.util.Log.w(
                "VestiaTrace",
                "WARN totalAmount mismatch (txId=$id) computed=$computed json=$total diff=$diff"
            )
        }
    }


    return Transaction(
        id = id,
        ownerId = ownerId,
        accountId = accountId,
        epochMs = epochMs,

        transactionType = txType,

        assetType = aType,
        assetInstrument = assetInstrument,
        unitType = unitType,

        quantity = qty,
        unitPrice = price,
        totalAmount = total,

        tags = tags?.takeIf { it.isNotBlank() }
    )
}

/* --------------------------
   Helpers (manuel, küçük)
   -------------------------- */

private fun parseBigDecimal(
    raw: String,
    fieldName: String,
    ctx: String
): BigDecimal {
    val s = raw.trim()
    require(s.isNotEmpty()) { "$fieldName boş olamaz ($ctx)" }
    return try {
        BigDecimal(s)
    } catch (e: NumberFormatException) {
        error("$fieldName BigDecimal parse edilemedi: '$raw' ($ctx)")
    }
}

private inline fun <reified T : Enum<T>> parseEnum(
    raw: String,
    fieldName: String,
    ctx: String
): T {
    val s = raw.trim()
    require(s.isNotEmpty()) { "$fieldName boş olamaz ($ctx)" }
    return try {
        enumValueOf<T>(s)
    } catch (e: IllegalArgumentException) {
        val allowed = enumValues<T>().joinToString(", ") { it.name }
        error("$fieldName enum parse edilemedi: '$raw' ($ctx). Allowed: $allowed")
    }
}
