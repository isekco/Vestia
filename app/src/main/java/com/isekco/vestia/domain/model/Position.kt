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
    val quantity: BigDecimal,
    val weightedAverageCost: BigDecimal,
    val totalCost: BigDecimal
)
