package com.isekco.vestia.domain.model

data class Transaction(
    val id: Long,
    val title: String,
    val amount: Double,
    val currency: String
)
