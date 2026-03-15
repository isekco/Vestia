package com.isekco.vestia.domain.model

/* Type of Asset */
enum class AssetType {
    XAU,     // Altın
    CASH,    // Nakit (TRY, USD, EUR vs)
}

/* Asset Instrument depending on the type of asset */
sealed interface AssetInstrument

enum class CashInstrument : AssetInstrument {
    TRY,
    USD,
    EUR,
    GBP
}

enum class XauInstrument : AssetInstrument {
    GRAM,
    CEYREK,
    YARIM,
    TAM
}

/* Asset key for filtering */
data class AssetKey(
    val assetType: AssetType,
    val assetInstrument: AssetInstrument,
)

