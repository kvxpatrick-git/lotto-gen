package com.hsmomo.lottogen.domain.strategy

import com.hsmomo.lottogen.domain.model.NumberSet
import kotlin.random.Random

class MixedStrategy : NumberGenerationStrategy {

    private var selectedNumbers: List<Int> = emptyList()

    fun setSelectedNumbers(numbers: List<Int>) {
        require(numbers.size <= 6) { "Cannot select more than 6 numbers" }
        require(numbers.all { it in 1..45 }) { "All numbers must be between 1 and 45" }
        require(numbers.toSet().size == numbers.size) { "Selected numbers must be unique" }
        selectedNumbers = numbers
    }

    override suspend fun generate(count: Int): List<NumberSet> {
        return (1..count).map {
            generateWithSelected()
        }
    }

    fun generateSingle(): NumberSet {
        return generateWithSelected()
    }

    private fun generateWithSelected(): NumberSet {
        if (selectedNumbers.size == 6) {
            return NumberSet(selectedNumbers.sorted())
        }

        val remaining = (1..45).filter { it !in selectedNumbers }
        val additionalCount = 6 - selectedNumbers.size
        val additional = remaining.shuffled(Random).take(additionalCount)

        return NumberSet((selectedNumbers + additional).sorted())
    }
}
