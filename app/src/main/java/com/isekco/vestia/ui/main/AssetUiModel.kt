package com.isekco.vestia.ui.main

data class AssetTypeUiModel(
    val assetTypeName: String,
    val totalQuantityText: String,
    val totalValueText: String,
    val isExpanded: Boolean,
    val instruments: List<AssetInstrumentUiModel>
)

data class AssetInstrumentUiModel(
    val instrumentName: String,
    val quantityText: String,
    val rateText: String,
    val totalValueText: String
)