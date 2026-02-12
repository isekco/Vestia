package com.isekco.vestia.domain.model

import java.math.BigDecimal

/**
 * Position = Ledger'dan türetilen (derived) portföy durumu.
 * JSON'dan direkt gelmez; PositionEngine tarafından hesaplanır.
 */
data class PositionKey(
    val ownerId: String,
    val accountId: String,
    val assetKey: String
)

data class Position(
    val key: PositionKey,

    /** Eldeki toplam adet/miktar */
    val quantity: BigDecimal,

    /** Ağırlıklı ortalama maliyet (WAC) */
    val weightedAverageCost: BigDecimal,

    /** Toplam maliyet = quantity * weightedAverageCost */
    val totalCost: BigDecimal
)
