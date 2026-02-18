package com.hsmomo.lottogen.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class Bookmark(
    val id: Long,
    val numbers: List<Int>,
    val createdAt: Long
) {
    val formattedDate: String
        get() {
            val instant = Instant.fromEpochMilliseconds(createdAt)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            return "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')} ${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
        }

    val numbersSortedText: String
        get() = numbers.sorted().joinToString(",")
}
