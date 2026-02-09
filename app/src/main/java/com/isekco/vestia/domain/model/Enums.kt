package com.isekco.vestia.domain.model

enum class Currency {
    TRY,
    USD,
    EUR,
    GBP
}
enum class TransactionType {
    BUY,
    SELL,
    DEPOSIT,
    WITHDRAW,
    GIFT_IN,
    GIFT_OUT
}

enum class AssetType {
    STOCK,   // Hisse
    FUND,    // Yatırım fonu
    XAU,     // Altın (generic)
    CASH,    // Nakit (TRY, USD, EUR vs)
    FX       // Döviz işlemleri (opsiyonel, ileride)
}

enum class UnitType {

    // Adet bazlı
    SHARE,        // Hisse adedi
    FUND_SHARE,   // Fon payı

    // Ağırlık
    GRAM,         // Altın (gram)
    OUNCE,        // Ons (ileride)

    // Para birimi bazlı
    TRY,
    USD,
    EUR
}
