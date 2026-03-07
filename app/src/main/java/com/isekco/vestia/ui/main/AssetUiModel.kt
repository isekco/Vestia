package com.isekco.vestia.ui.main

import java.math.BigDecimal

data class AssetUiModel(
    val assetKey: String,
    val assetLabel: String,
    val quantity: BigDecimal,
    val tryRate: BigDecimal,
    val totalValueTry: BigDecimal
)