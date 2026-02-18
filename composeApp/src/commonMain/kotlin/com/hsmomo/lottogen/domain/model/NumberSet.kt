package com.hsmomo.lottogen.domain.model

data class NumberSet(
    val numbers: List<Int>,
    val isBookmarked: Boolean = false
) {
    val sortedNumbers: List<Int>
        get() = numbers.sorted()

    val numbersSortedText: String
        get() = sortedNumbers.joinToString(",")

    companion object {
        fun create(numbers: List<Int>): NumberSet {
            require(numbers.size == 6) { "Numbers must contain exactly 6 elements" }
            require(numbers.all { it in 1..45 }) { "All numbers must be between 1 and 45" }
            require(numbers.toSet().size == 6) { "All numbers must be unique" }
            return NumberSet(numbers.sorted())
        }
    }
}
