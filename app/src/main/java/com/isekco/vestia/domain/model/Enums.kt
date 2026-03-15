package com.isekco.vestia.domain.model

enum class Currency {

    TRY,
    USD,
    EUR,
    GBP,
    XAU
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


