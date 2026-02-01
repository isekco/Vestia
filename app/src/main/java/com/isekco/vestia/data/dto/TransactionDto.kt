package com.isekco.vestia.data.dto

import com.isekco.vestia.domain.model.Transaction

data class TransactionDto(
    val id: Long,
    val description: String,
    val amount_str: String,
    val ccy: String
)

fun TransactionDto.toDomain(): Transaction {
    val amount = amount_str.toDouble()

    return Transaction(
        id = id,
        title = description,
        amount = amount,
        currency = ccy
    )
}
