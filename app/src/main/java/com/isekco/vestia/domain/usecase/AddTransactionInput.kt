package com.isekco.vestia.domain.usecase

/**
 * UI -> UseCase arasında taşınan input model.
 * Domain Transaction değildir.
 */
data class AddTransactionInput(
    val ownerId: String,
    val accountId: String,

    val transactionType: String, // "BUY" / "SELL" ...

    /**
     * JSON sözleşmesi ile uyumlu token:
     * - "XAU" (altın)
     * - "USD"/"EUR"/"TRY"/"GBP" (cash)
     */
    val assetTypeToken: String,

    val quantity: String,
    val unitPrice: String,
    val priceCurrency: String,

    val epochMs: Long? = null,
    val tags: String? = null
)