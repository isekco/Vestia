package com.isekco.vestia.domain.model

import java.math.BigDecimal

data class PositionKey(
    val ownerId: String,
    val accountId: String,
    val assetKey: AssetKey
)

data class Position(
    val key: PositionKey,
    val quantity: BigDecimal, /* Total amount of asset */
    val wac: BigDecimal, /* Weighted Average Cost of asset in terms of TRY (base currency) */
    val totalCost: BigDecimal /* Total cost of the asset in terms of TRY (base currency) */
)

data class ValuedPosition(
    val position: Position,
    val rateToBase: BigDecimal?,
    val marketValue: BigDecimal?
)