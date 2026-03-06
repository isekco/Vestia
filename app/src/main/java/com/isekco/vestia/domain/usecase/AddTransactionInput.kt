package com.isekco.vestia.domain.usecase

data class AddTransactionInput(
    val ownerId: String,
    val accountId: String,
    val transactionType: String,   // BUY / SELL
    val quantity: String,
    val unitPrice: String,         // TRY bazlı
    val epochMs: Long,             // datepicker seçimi
    val tags: String?
)