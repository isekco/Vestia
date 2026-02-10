package com.isekco.vestia.domain.model

enum class Currency {
    TRY,
    USD,
    EUR,
    GBP
}
enum class TransactionDirection {
    IN,
    OUT
}

enum class TransactionType(val direction: TransactionDirection) {
    BUY(TransactionDirection.IN),
    SELL(TransactionDirection.OUT),
    GIFT_IN(TransactionDirection.IN),
    GIFT_OUT(TransactionDirection.OUT)
}

enum class AssetType {

    XAU,     // Altın (generic)
    CASH,    // Nakit (TRY, USD, EUR vs)
    FX       // Döviz işlemleri (opsiyonel, ileride)
}

enum class UnitType {
    // Ağırlık
    GRAM,         // Altın (gram)
    OUNCE,        // Ons (ileride)

    // Para birimi bazlı
    TRY,
    USD,
    EUR,
    GBP
}
