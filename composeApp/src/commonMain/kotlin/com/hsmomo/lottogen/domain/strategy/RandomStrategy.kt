package com.hsmomo.lottogen.domain.strategy

import com.hsmomo.lottogen.domain.model.NumberSet
import kotlin.random.Random

class RandomStrategy : NumberGenerationStrategy {

    override suspend fun generate(count: Int): List<NumberSet> {
        return (1..count).map {
            val numbers = (1..45).shuffled(Random).take(6).sorted()
            NumberSet(numbers)
        }
    }

    fun generateSingleSet(): NumberSet {
        val numbers = (1..45).shuffled(Random).take(6).sorted()
        return NumberSet(numbers)
    }

    fun fillRemaining(selected: List<Int>): NumberSet {
        require(selected.size <= 6) { "Selected numbers cannot exceed 6" }
        require(selected.all { it in 1..45 }) { "All numbers must be between 1 and 45" }
        require(selected.toSet().size == selected.size) { "Selected numbers must be unique" }

        val remaining = (1..45).filter { it !in selected }
        val additionalCount = 6 - selected.size
        val additional = remaining.shuffled(Random).take(additionalCount)

        return NumberSet((selected + additional).sorted())
    }
}
