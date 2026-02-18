package com.hsmomo.lottogen.domain.model

data class WinningDraw(
    val drawNo: Int,
    val drawDate: String,
    val numbers: List<Int>,
    val bonus: Int,
    val firstPrizeAmount: Long
) {
    val formattedPrizeAmount: String
        get() = "${firstPrizeAmount.formatWithCommas()}원"
}

fun Long.formatWithCommas(): String {
    return toString().reversed().chunked(3).joinToString(",").reversed()
}
