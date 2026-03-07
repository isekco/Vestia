package com.isekco.vestia.ui.main

import java.math.BigDecimal

data class AssetUiModel(
    val assetKey: String,
    val quantity: BigDecimal,
    val tryRate: BigDecimal,
    val totalValueTry: BigDecimal,
    val isSelected: Boolean = false
)