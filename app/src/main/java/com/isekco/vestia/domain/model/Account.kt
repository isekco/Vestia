package com.isekco.vestia.domain.model

data class Account(
    val id: String,
    val ownerId: String,
    val name: String,
    val currency: Currency // hesabın native/settlement para birimi
)